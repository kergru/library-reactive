package org.kergru.library.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

  @GetMapping("/error/403")
  public String handle403() {
    return "error/403";
  }
}
