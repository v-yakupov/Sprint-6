package ru.sber.sbermvc.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import ru.sber.sbermvc.service.Record
import ru.sber.sbermvc.service.RecordService
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api")
class RestController {
    @Autowired
    private lateinit var recordService: RecordService

    @GetMapping("/list")
    fun getAllRecords(): ConcurrentHashMap<Long, Record> {
        return recordService.getAll()
    }

    @PostMapping("/add")
    fun addRecordPost(@RequestBody record: Record) {
        recordService.add(record)
    }

    @GetMapping("/{id}/view")
    fun getRecord(@PathVariable id: Long): Record? {
        return recordService.get(id)
    }

    @PostMapping("/{id}/edit")
    fun editRecordPost(@PathVariable id: Long, @RequestBody record: Record) {
        recordService.update(id, record)
    }

    @GetMapping("/{id}/delete")
    fun removeRecord(@PathVariable id: Long) {
        recordService.remove(id)
    }
}