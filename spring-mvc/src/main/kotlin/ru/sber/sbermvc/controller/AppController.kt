package ru.sber.sbermvc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import ru.sber.sbermvc.service.Record
import ru.sber.sbermvc.service.RecordService

@Controller
@RequestMapping("/app")
class AppController {
    @Autowired
    private lateinit var recordService: RecordService

    @GetMapping("/list")
    fun getAllRecords(@RequestParam(required = false) id: Long?,
                      @RequestParam(required = false) name: String?,
                      @RequestParam(required = false) address: String?,
                      model: Model): String {
        if (id != null || name != null || address != null)
            model.addAttribute("records", recordService.getAll(id, name, address))
        else
            model.addAttribute("records", recordService.getAll())
        return "all"
    }

    @GetMapping("/add")
    fun addRecordGet(): String {
        return "add"
    }
    @PostMapping("/add")
    fun addRecordPost(@ModelAttribute("record") record: Record, model: Model): String {
        recordService.add(record)
        return "redirect:/app/list"
    }

    @GetMapping("/{id}/view")
    fun getRecord(@PathVariable id: Long, model: Model): String {
        model.addAttribute("record", recordService.get(id))
        return "record"
    }

    @GetMapping("/{id}/edit")
    fun editRecordGet(@PathVariable id: Long, model: Model): String {
        model.addAttribute("record", recordService.get(id))
        return "edit"
    }
    @PostMapping("/{id}/edit")
    fun editRecordPost(@PathVariable id: Long, @ModelAttribute("record") record: Record): String {
        recordService.update(id, record)
        return "redirect:/app/list"
    }

    @GetMapping("/{id}/delete")
    fun removeRecord(@PathVariable id: Long): String {
        recordService.remove(id)
        return "redirect:/app/list"
    }
}