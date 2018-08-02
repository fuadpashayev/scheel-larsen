package com.scheellarsen

class Products {
    var Color_count:Int? = null
    var Descriptions:String? = null
    var Img:String? = null
    var Img_count:Int? = null
    var Index:String? = null
    var Name:String? = null
    var Price:String? = null
    var Size_count:Int? = null
    var Url:String? = null

    constructor(){

    }


    constructor(Color_count:Int?,Descriptions:String?,Img:String?,Img_count:Int?,Index:String?,Price:String?,Size_count:Int?,Url:String?,Name:String?) {
        this.Color_count = Color_count
        this.Descriptions = Descriptions
        this.Img = Img
        this.Img_count = Img_count
        this.Index = Index
        this.Price = Price
        this.Size_count = Size_count
        this.Url = Url
        this.Name = Name


    }

}