package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
@Controller
public class ViewController {
	
    @RequestMapping("/main.mc")
    public ModelAndView main() {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("main");  
            return mv;
    }
    // 네비게이션 바에서 Chart탭 클릭하여 Chart 페이지로 이동합니다.
    @RequestMapping("/chart.mc")
    public ModelAndView chart() {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("chart");  
            return mv;
    }
    // Highchart 테스트용입니다.
    @RequestMapping("/test.mc")
    public ModelAndView test() {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("test");  
            return mv;
    }

    // Highchart 테스트용입니다.
    @RequestMapping("/test2.mc")
    public ModelAndView test2() {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("test2");  
            return mv;
    }
}
