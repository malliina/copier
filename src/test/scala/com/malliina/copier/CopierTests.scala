package com.malliina.copier

import cats.effect.IO
import com.malliina.copier.Copier.getClass

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
