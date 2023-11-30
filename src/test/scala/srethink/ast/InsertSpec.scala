package srethink.ast

import play.api.rql._
import srethink._
import java.util.Date
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global

class InsertSpec extends RethinkSpec with WithData {
  test("insert scala values") {

    val items = (1 to 1).map(i =>
      book(i).copy(coAuthors = CoAuthor("foo", Seq("bar", "baz")) :: Nil)
    )
    books.insert(items).runAs[InsertResult].map { ir =>
      assertEquals(ir.inserted, 1)
    }
  }

  test("insert JsValues value") {
    val items = Seq(Json.parse(json))
    books.insertJS(items).runAs[InsertResult].map { ir =>
      assertEquals(ir.inserted, 1)
    }
  }

  val json = """
{"id":324,"formId":"596cb424-13ab-4e70-b8e0-9487fdfb06c6","openid":"oBG0juPoz5nn9sqQ5wuAbL1O5d1U","weixinUserInfo":{"openid":"oBG0juPoz5nn9sqQ5wuAbL1O5d1U","nickname":"yison","sex":1,"province":"浙江","city":"杭州","country":"中国","headimgurl":"http://wx.qlogo.cn/mmopen/g1DC2XPOBX8MhCmmkqdicQmsbmJjXRpYCohibItdULqicmq4KUtSj0blNTzibPEcu38aASia1iaXxtX17z7s0o4tMI3O6MW2DwOppA/0"},"answers":[2,[{"fieldId":101,"fieldValues":[2,[1,2,5]]}]],"isNotified":false,"isRead":false,"isDeleted":false,"managers":[2,["o3i5rs4yUBbW9fJ2J-ag8HXRqGR4","o3i5rszpoC9aS7rDP_QRQLP1fV6A"]],"createTime":{"$reql_type$":"TIME","epoch_time":1442301772.134,"timezone":"+00:00"},"updateTime":{"$reql_type$":"TIME","epoch_time":1442301772.134,"timezone":"+00:00"}}
 """
}

case class WxFormParticipant(
  id: Long,
  formId: String,
  openid: String,
  weixinUserInfo: Option[JsValue],
  answers: Seq[JsValue],
  isNotified: Boolean,
  isRead: Boolean,
  isDeleted: Boolean,
  managers: Seq[String],
  createTime: Date,
  updateTime: Date
)
