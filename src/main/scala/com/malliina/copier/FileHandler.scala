package com.malliina.copier

import cats.effect.Async
import cats.implicits.catsSyntaxFlatMapOps
import cats.syntax.all.{catsSyntaxApplicativeError, toFlatMapOps, toFunctorOps}
import fs2.Stream
import fs2.io.file.{Files, Path}

import java.nio.file.FileSystemException

trait FileHandler[F[_]: {Async, Files}]:
  private val log = AppLogger(getClass)
  val S = Async[F]
  val F = Files[F]

  def isWritable(to: Path): F[Path] = F
    .isWritable(to)
    .flatMap: isWritable =>
      if isWritable then writeLog(s"Directory '$to' is writable.") >> S.pure(to)
      else S.raiseError(FileSystemException(s"Not writable: '$to'."))

  def listFiles(from: Path, include: Path => Boolean): Stream[F, Path] = F
    .walk(from)
    .evalFilter(p => F.isRegularFile(p))
    .filter(p => include(p))

  def destFile(src: Path): Path = src.fileName

  def processAll(
    from: Path,
    to: Path,
    include: Path => Boolean
  ): Stream[F, Either[Throwable, Path]] =
    listFiles(from, include)
      .map(src => (src, to.resolve(destFile(src))))
      .evalFilter((_, dest) => F.exists(dest).map(e => !e))
      .parEvalMap(2): (src, dest) =>
        process(src, dest)
          .map[Either[Throwable, Path]](_ => Right(dest))
          .handleError(err => Left(err))

  def process(src: Path, dest: Path): F[Unit]

  def writeLog(msg: String): F[Unit] = S.delay(log.info(msg))
