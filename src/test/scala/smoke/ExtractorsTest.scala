package smoke

import org.scalatest.FunSpec

class ExtractorsTest extends FunSpec {
  describe("Test Seg") {
    it("should segment cleanly") {
      val list = Seg.unapply("/1/2/3/4/5/6/7")
      expect(7)(list.get.size)
    }

    it("should segment removing empty pieces") {
      val list = Seg.unapply("/1/2/3/4/5/6//7")
      expect(7)(list.get.size)
    }

    it("should segment removing many empty pieces") {
      val list = Seg.unapply("/1/2/3/4/5/6///7")
      expect(7)(list.get.size)
    }

    it("should segment removing empty pieces and multiple leading '/'") {
      val Seg(list) = "//1/2/3/4/5/6///7"
      expect(7)(list.size)
    }
  }

  describe("Test FileExtension") {
    it("should return a file extension") {
      expect(Some("m3u8"))(FileExtension.unapply("foo.m3u8"))
    }

    it("should not return a file extension") {
      expect(None)(FileExtension.unapply("foo"))
    }
  }

  describe("Test Method") {
    it("apply should return whether a request matches that method") {
      val req = new test.TestRequest("http://test.com")
      assert(GET(req))
      assert(!POST(req))
      assert(!PUT(req))
    }
  }
}
