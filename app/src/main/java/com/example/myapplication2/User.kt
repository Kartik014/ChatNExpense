package com.example.startup

class User {
    var name: String?=null
    var email: String?=null
    var uid: String?=null
    var PhoneNo: String?=null
    var groupname: String?=null
    var imageUrl: String?=null
    var fcmToken: String?=null

    constructor(){}

    constructor(name: String?, email: String?, uid: String?, PhoneNo: String?, groupname: String?, imageUrl: String?,fcmToken: String?){
        this.name= name
        this.email= email
        this.uid= uid
        this.PhoneNo= PhoneNo
        this.groupname= groupname
        this.imageUrl= imageUrl
        this.fcmToken= fcmToken
    }
}