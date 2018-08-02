package com.scheellarsen

class Colors {
    var Color:String = ""
    var list:ArrayList<String>?= arrayListOf()
    constructor(){

    }


    constructor(Color: String) {
        this.Color = Color
        this.list!!.add(this.Color)
    }

}