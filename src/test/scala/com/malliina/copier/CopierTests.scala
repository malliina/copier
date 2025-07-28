package com.malliina.copier

import cats.effect.IO
import fs2.io.file.Path

import scala.concurrent.duration.{Duration, DurationInt}

class CopierTests extends munit.CatsEffectSuite:
  private val log = AppLogger(getClass)

  override def munitIOTimeout: Duration = 24.hours

  test("List files".ignore):
    val task = Copier.fitcamx[IO].srcFiles.compile.toList
    task.map: files =>
      log.info(s"$files")
      assertEquals(1, 1)

  test("Copy files".ignore):
    val copier = Copier.dji[IO]
    copier.copyToList
      .map: ps =>
        val paths = ps.collect:
          case Right(path) => path
        log.info(s"Wrote ${paths.size} files: ${paths.mkString(", ")}")
        assertEquals(1, 1)

  test("Encode file".ignore):
    val encoder = VideoEncoder[IO]
    val dir = Path("/Users/michael.skogberg/fitcamx")
    encoder
      .encode(
        dir.resolve("20240901070427_011864.TS"),
        dir.resolve("20240901070427_011864-encoded.mp4")
      )
      .use: p =>
        for out <- p.std
            .evalTap(str => IO.println(str))
            .compile
            .toList
        yield assertEquals(0, 0)
