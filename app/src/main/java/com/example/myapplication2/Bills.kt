package com.example.myapplication2

class Bills {

    var amount: String?= null
    var senderId: String?= null

    constructor(){}

    constructor(amount: String?, senderId: String?){
        this.amount= amount
        this.senderId= senderId
    }

}