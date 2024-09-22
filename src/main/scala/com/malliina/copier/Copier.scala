package com.malliina.copier

import cats.effect.Async
import cats.syntax.all.{catsSyntaxApplicativeError, toFlatMapOps, toFunctorOps}
import com.malliina.copier.Copier.log
import com.malliina.logback.LogbackUtils
import fs2.Stream
import fs2.io.file.{Files, Path}

import java.nio.file.FileSystemException

object Copier:
  private val log = AppLogger(getClass)
  LogbackUtils.init()

  def fitcamx[F[_]: Files: Async] =
    Copier[F](
      from = Path("/Volumes/Untitled"),
      to = Path("/Volumes/pi/Fitcamx"),
      p => p.extName == ".TS"
    )

  def dji[F[_]: Files: Async] =
    Copier[F](
      from = Path("/Volumes/Untitled"),
      to = Path("/Volumes/pi/DJI"),
      p => Seq(".MP4", ".JPG").contains(p.extName)
    )

class Copier[F[_]: Files: Async](
  from: Path,
  to: Path,
  include: Path => Boolean
):
  private val F = Files[F]
  private val S = Async[F]

  val srcFiles: Stream[F, Path] = F
    .walk(from)
    .evalFilter(p => F.isRegularFile(p))
    .filter(p => include(p))

  def copyToList: F[List[Either[Throwable, Path]]] =
    copy.compile.toList

  def copy: Stream[F, Either[Throwable, Path]] =
    Stream.eval(checkWritable) >> copyFiles

  private def checkWritable: F[Path] = F
    .isWritable(to)
    .flatMap: isWritable =>
      if isWritable then S.pure(to) else S.raiseError(FileSystemException(s"Not writable: '$to'."))

  private def copyFiles: Stream[F, Either[Throwable, Path]] = srcFiles
    .map(src => (src, to.resolve(src.fileName)))
    .evalFilter((_, dest) => F.exists(dest).map(e => !e))
    .parEvalMap(2): (src, dest) =>
      for
        _ <- writeLog(s"Copying $src to $dest...")
        d <- F
          .copy(src, dest)
          .map[Either[Throwable, Path]](_ => Right(dest))
          .handleError(err => Left(err))
        _ <- writeLog(
          d.fold(err => s"Failed to copy $src to $dest. $err", f => s"Copied $src to $f.")
        )
      yield d

  private def writeLog(msg: String): F[Unit] = S.delay(log.info(msg))
