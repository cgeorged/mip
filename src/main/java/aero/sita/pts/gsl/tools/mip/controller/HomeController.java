package aero.sita.pts.gsl.tools.mip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class HomeController {


    
    
    @RequestMapping(value = "/")  
    public String index() {
        return "index";  
    }

    
    
}
