package com.malliina.copier

import cats.effect.Async
import cats.syntax.all.{catsSyntaxApplicativeError, toFlatMapOps, toFunctorOps}
import com.malliina.logback.LogbackUtils
import fs2.Stream
import fs2.io.file.{Files, Path}

object Copier:
  private val log = AppLogger(getClass)
  LogbackUtils.init()

  def fitcamx[F[_]: {Files, Async}] =
    Copier[F](
      from = Path("/Volumes/Untitled"),
      to = Path("/Volumes/pi/Fitcamx"),
      p => p.extName == ".TS"
    )

  def dji[F[_]: {Files, Async}] =
    Copier[F](
      from = Path("/Volumes/Untitled"),
      to = Path("/Volumes/pi/DJI"),
      p => Seq(".MP4", ".JPG").contains(p.extName)
    )

class Copier[F[_]: {Files, Async}](
  from: Path,
  to: Path,
  include: Path => Boolean
) extends FileHandler[F]:
  private val S = Async[F]

  val srcFiles = listFiles(from, include)

  def copyToList: F[List[Either[Throwable, Path]]] =
    copy.compile.toList

  def copy: Stream[F, Either[Throwable, Path]] =
    Stream.eval(isWritable(to)) >> copyFiles

  private def copyFiles: Stream[F, Either[Throwable, Path]] = processAll(from, to, include)

  override def process(src: Path, dest: Path): F[Unit] =
    for
      _ <- writeLog(s"Copying $src to $dest...")
      d <- F
        .copy(src, dest)
        .flatMap: _ =>
          writeLog(s"Copied $src to $dest.")
        .onError: err =>
          writeLog(s"Failed to copy $src to $dest. $err")
    yield d
