package com.soundcloud.sketchy.events

import java.util.Date

import java.text.SimpleDateFormat
import org.scalatest.{ FlatSpec, BeforeAndAfterEach }

import com.soundcloud.sketchy.SpecHelper

/**
 * Test for signals
 */
class SketchySignalTest extends FlatSpec with BeforeAndAfterEach with SpecHelper {
  behavior of "A sketchy signal"

  var now: Date = _
  val fmt = "yyyy/MM/dd HH:mm:ss ZZZZZ"

  override def beforeEach() {
    now = new SimpleDateFormat(fmt).parse("2012/05/23 16:45:15 +0000")
  }

  it should "serialize to JSON" in {
    val junk = SketchySignal(1, "Message", List(1), "Junk", 1.0, now)
    assert(junk.json + "\n" === fixtures("signal", "junk"))
  }

  it should "deserialize from JSON" in {
    val expected = SketchySignal(1, "Message", List(1), "Junk", 1.0, now)
    assert(expected ===
      Event.fromJson[SketchySignal](fixtures("signal", "junk")))
  }
}
