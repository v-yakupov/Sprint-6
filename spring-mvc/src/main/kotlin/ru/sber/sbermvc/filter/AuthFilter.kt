package ru.sber.sbermvc.filter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebFilter(urlPatterns = ["/app/*", "/api/*"])
class AuthFilter : Filter {
    override fun doFilter(p0: ServletRequest, p1: ServletResponse, p2: FilterChain) {
        val req = p0 as HttpServletRequest
        val res = p1 as HttpServletResponse
        var isAllowed = false

        if (!req.cookies.isNullOrEmpty()) {
            req.cookies.forEach { cookie ->
                if (cookie.name == "auth" && cookie.value.toLong() < System.currentTimeMillis()) {
                    isAllowed = true
                }
            }
        }
        if (isAllowed)
            p2.doFilter(req, res)
        else
            res.sendRedirect("/login")
    }
}