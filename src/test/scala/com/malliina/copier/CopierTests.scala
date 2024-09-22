package com.malliina.copier

import cats.effect.IO

import scala.concurrent.duration.{Duration, DurationInt}

class CopierTests extends munit.CatsEffectSuite:
  override def munitIOTimeout: Duration = 24.hours

  test("can run test".ignore):
    val task = Copier.fitcamx[IO].srcFiles.compile.toList
    task.map: files =>
      println(files)
      assertEquals(1, 1)

  test("Copy files".ignore):
    val copier = Copier.dji[IO]
    copier.copyToList
      .map: ps =>
        val paths = ps.collect:
          case Right(path) => path
        println(s"Wrote ${paths.size} files: ${paths.mkString(", ")}")
        assertEquals(1, 1)
