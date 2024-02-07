package com.example.newp

//chat메세지 데이터 클래스
data class Message(
    val senderId: String = "",          //받는 사람 id
    val receiverId: String = "",      //보낸 사람 id
    val content: String = "",    //메세지 내용
    val title: String = "",   //글 제목
    val time: String = "",   //시간
    val read: Boolean? = false // 읽음 여부
)