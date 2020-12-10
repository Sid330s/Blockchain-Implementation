var express= require("express");
var app=express();
var bodyparser=  require("body-parser");
var axios= require("axios");

app.set("view engine","ejs");
app.use(bodyparser.urlencoded({extended:true}));


app.get("/",function(req,response)
{
    axios.get("http://127.0.0.1:8080/chain")
    .then(res=>
        {
            response.render("index",{data:res.data})
        })
        .catch(err=>{
            console.log("Error",err);
        })
});
app.post("/submit",function(req,res)
{
    var author=req.body.author;
    var content=req.body.content;
    var transaction={
        author:author,
        content:content};
    
    axios.post("http://127.0.0.1:8080/new_transaction",transaction);
    res.redirect("/");
});


app.listen(8000,function(){console.log("Server has started")});






