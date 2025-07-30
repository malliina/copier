package com.malliina.copier

import cats.effect.{Async, Resource}
import cats.syntax.all.{catsSyntaxApplicativeError, toFlatMapOps, toFunctorOps}
import com.malliina.copier.VideoEncoder.log
import com.malliina.logback.LogbackUtils
import fs2.io.file.{Files, Path}
import fs2.io.process.{ProcessBuilder, Processes}
import fs2.text

object DirEncoder:
  private val log = AppLogger(getClass)
  LogbackUtils.init()

  def fitcamx[F[_]: {Async, Processes}] =
    DirEncoder[F](
      Path("/Volumes/pi/Fitcamx"),
      Path("/Volumes/pi/Fitcamx/encoded"),
      p => p.extName == ".TS"
    )

class DirEncoder[F[_]: {Async, Files, Processes}](
  from: Path,
  to: Path,
  include: Path => Boolean
) extends FileHandler[F]:
  private val enc = VideoEncoder[F]

  def encodeAll = processAll(from, to, include)

  override def destFile(src: Path) = Path(src.fileName.toString + ".mp4")

  override def process(src: Path, dest: Path): F[Unit] =
    for
      _ <- writeLog(s"Encoding $src to $dest...")
      res <- enc
        .encodeAwait(src, dest)
        .flatMap: res =>
          if res.exitValue == 0 then S.pure(res)
          else S.raiseError(Exception(s"Unexpected exit value: ${res.exitValue}.\n${res.stderr}"))
        .flatMap: _ =>
          writeLog(s"Encoded $src to $dest.")
        .onError: err =>
          writeLog(s"Failed to encode $src to $dest. $err")
    yield res

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
