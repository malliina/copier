package com.malliina.copier

import cats.effect.{Async, Concurrent, Resource}
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import com.malliina.copier.VideoEncoder.log
import com.malliina.logback.LogbackUtils
import fs2.io.file.Path
import fs2.io.process.{ProcessBuilder, Processes}
import fs2.{Stream, text}

case class ProcessResult(exitValue: Int, stdout: String, stderr: String)

case class LaunchedProcess[F[_]: Concurrent](
  exitValue: F[Int],
  stdout: Stream[F, String],
  stderr: Stream[F, String]
):
  val std = stdout.merge(stderr)
  def await = for
    exit <- exitValue
    out <- stdout.compile.string
    err <- stderr.compile.string
  yield ProcessResult(exit, out, err)

object VideoEncoder:
  private val log = AppLogger(getClass)
  LogbackUtils.init()

class VideoEncoder[F[_]: {Async, Processes}]:
  def encodeAwait(from: Path, to: Path): F[ProcessResult] =
    encode(from, to).use(_.await)

  def encode(from: Path, to: Path): Resource[F, LaunchedProcess[F]] =
    val pb = ProcessBuilder(
      "ffmpeg",
      "-i",
      from.absolute.toString,
      "-vf",
      "scale=1920:-2",
      "-vcodec",
      "libx264",
      "-crf",
      "30",
      "-y",
      to.absolute.toString
    )
    pb.spawn.evalMap: p =>
      val cmd = (Seq(pb.command) ++ pb.args).mkString(" ")
      writeLog(s"Running '$cmd'...").map: _ =>
        LaunchedProcess(
          p.exitValue,
          p.stdout.through(text.utf8.decode).through(text.lines),
          p.stderr.through(text.utf8.decode).through(text.lines)
        )

  private def writeLog(msg: String): F[Unit] = Async[F].delay(log.info(msg))
