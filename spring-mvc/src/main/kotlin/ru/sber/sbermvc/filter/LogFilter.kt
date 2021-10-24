package ru.sber.sbermvc.filter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest

@WebFilter(urlPatterns = ["/*"])
class LogFilter : Filter {
    override fun doFilter(p0: ServletRequest, p1: ServletResponse, p2: FilterChain) {
        if (p0 is HttpServletRequest)
            println("[LOG] ${p0.remoteAddr}: ${p0.method} ${p0.requestURI}")
        p2.doFilter(p0, p1)
    }
}