package com.malliina.copier

import cats.effect.Concurrent
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import fs2.Stream

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
