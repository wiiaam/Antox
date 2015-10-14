package chat.tox.antox.tests

import android.preference.PreferenceManager
import android.support.test.runner.AndroidJUnit4
import android.test.{AndroidTestCase, RenamingDelegatingContext}
import chat.tox.antox.data.{State, AntoxDB}
import chat.tox.antox.tox.{ToxDataFile, ToxSingleton}
import chat.tox.antox.utils.{Options, SelfKey}
import chat.tox.antox.wrapper._
import im.tox.tox4j.core.enums.{ToxSavedataType, ToxUserStatus, ToxMessageType}
import im.tox.tox4j.core.options.{SaveDataOptions, ToxOptions}
import im.tox.tox4j.exceptions.ToxException
import org.junit.Assert._
import org.junit.runner.RunWith
import org.junit.{After, Before, Test}

import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[AndroidJUnit4])
class AntoxDBTest extends AndroidTestCase {

  private var db: AntoxDB = _

  val context = new RenamingDelegatingContext(getContext, "test_")

  @Before
  override def setUp(): Unit = {
    super.setUp()
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    db = new AntoxDB(context, "jim", selfKey)
  }

  @After
  override def tearDown(): Unit = {
    super.tearDown()
  }

  val selfKey = new SelfKey("A1C7C6D463F7E6211BB3DC886B41568346EAA76FFDC11C56803F107774CC5124")

  val key = new FriendKey("DC11C56803F107774CC5124A1C7C6D463F7E6211BB3DC886B41568346EAA76FF")
  val name = "Steve Appleseed"
  val alias = "Steve"
  val statusMessage = "This is my status"
  
  @Test
  def testAddFriend(): Unit = {
    db.addFriend(key, name, alias, statusMessage)

    db.friendInfoList.subscribe(friendList => {
      assert(friendList.size == 1)
      assert(friendList.exists(_.name equals name))
      assert(friendList.exists(_.alias equals alias))
      assert(friendList.exists(_.getAliasOrName equals alias))
      assert(friendList.exists(_.statusMessage equals statusMessage))
      assert(friendList.exists(_.key equals key))
    })
  }

  @Test
  def testLastMessages(): Unit = {
    db.addFriend(key, name, alias, statusMessage)

    val numChanges = 5
    var number = 0
    db.messageListObservable(Some(key))
      .subscribe(messages => {
      assertEquals(messages, ArrayBuffer.empty)
      number += 1
    })

    val numMessages = 1
    db.addMessage(-1, key, selfKey, "asdf", "test", hasBeenReceived = false, hasBeenRead = false, successfullySent = true, ToxMessageType.NORMAL)

    db.lastMessages
      .subscribe(messages => {
      assertEquals(messages.size, numMessages)
      assert(messages.exists(_.message == "test"))
      
      number += 1
    })

    assertEquals(number, numChanges)
  }

  @Test
  def testUnreadMessages(): Unit ={
    db.markIncomingMessagesRead(key)

    db.addMessage(-1, key, selfKey, "asdf", "test", hasBeenReceived = false, hasBeenRead = false, successfullySent = true, ToxMessageType.NORMAL)
    db.addMessage(-1, key, selfKey, "asdf", "test", hasBeenReceived = false, hasBeenRead = false, successfullySent = true, ToxMessageType.NORMAL)
    db.addMessage(-1, key, selfKey, "asdf", "test", hasBeenReceived = false, hasBeenRead = false, successfullySent = true, ToxMessageType.NORMAL)
    db.addMessage(-1, key, selfKey, "asdf", "test", hasBeenReceived = false, hasBeenRead = false, successfullySent = true, ToxMessageType.NORMAL)
    db.addMessage(-1, key, selfKey, "asdf", "test", hasBeenReceived = false, hasBeenRead = false, successfullySent = true, ToxMessageType.NORMAL)

    assertEquals(5, db.getUnreadCounts(key))
  }




}