package br.ufc.insightlab.linkedgraphast

package object experiments {
  implicit class NaNAsZero(val value: Double) extends AnyVal {
    def nanAsZero: Double =
      if(value.isNaN) 0
      else value
  }
}
