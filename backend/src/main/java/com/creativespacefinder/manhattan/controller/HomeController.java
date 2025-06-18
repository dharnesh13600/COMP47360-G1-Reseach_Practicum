package com.creativespacefinder.manhattan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// HomeController class is specified to control the mapping of the application's home/root page
@Controller
public class HomeController {
    // Where the root is inputted in the domain mapping, return index.html to the web page
    @RequestMapping("/")
    public String index(){
        return "index";
    }
}
