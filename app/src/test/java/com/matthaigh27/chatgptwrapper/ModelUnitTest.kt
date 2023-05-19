package com.matthaigh27.chatgptwrapper

import com.matthaigh27.chatgptwrapper.models.ChatModel
import com.matthaigh27.chatgptwrapper.models.ImageTableModel
import com.matthaigh27.chatgptwrapper.models.RequestBodyModel
import com.matthaigh27.chatgptwrapper.utils.ReqType
import org.junit.Test

import org.junit.Assert.*

class ModelUnitTest {
    @Test
    fun chatModel_isCorrect() {
        val testMessage = "TestMessage"
        val testImageName = "TestImageName"
        val testByteArraySize = 10
        val testFeedback = 0
        val testIsMe = true

        val model = ChatModel()
        model.message = testMessage
        model.isMe = testIsMe
        model.feedback = testFeedback
        model.imageName = testImageName
        model.image = ByteArray(testByteArraySize);

        assertEquals(model.message, testMessage)
        assertEquals(model.isMe, testIsMe)
        assertEquals(model.feedback, testFeedback)
        assertEquals(model.imageName, testImageName)
        assertEquals(model.image!!.size, testByteArraySize)
    }

//    @Test
//    fun requestBodyModel_isCorrect() {
//        val testMessage = "TestMessage"
//        val testType = "TestImageName"
//        val testToken = "TestToken"
//        val testUUID = "TestUUID"
//        val testImageName = "TestImageName"
//
//        val model = RequestBodyModel.Builder()
//            .message(testMessage)
//            .type(testType)
//            .token(testToken)
//            .uuid(testUUID)
//            .imageName(testImageName)
//            .build()
//
//        assertEquals(model.message, testMessage)
//        assertEquals(model.type, testType)
//        assertEquals(model.token, testToken)
//        assertEquals(model.uuid, testUUID)
//        assertEquals(model.imageName, testImageName)
//    }

    @Test
    fun imageTableModel_isCorrect() {
        val testId = "TestId"
        val testPath = "TestPath"

        val model = ImageTableModel(testId, testPath)

        assertEquals(model.id, testId)
        assertEquals(model.path, testPath)
    }
}