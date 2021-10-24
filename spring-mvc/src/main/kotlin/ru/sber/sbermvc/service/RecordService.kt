package ru.sber.sbermvc.service

import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
@Scope(SCOPE_SINGLETON)
class RecordService {
    private val recordBook = ConcurrentHashMap<Long, Record>()

    fun getAll(): ConcurrentHashMap<Long, Record> {
        return recordBook
    }

    fun getAll(id: Long?, name: String?, address: String?): ConcurrentHashMap<Long, Record> {
        val sortedBook = ConcurrentHashMap<Long, Record>()
        recordBook.forEach { record ->
            if (record.key == id || record.value.name == name || record.value.address == address)
                sortedBook[record.key] = record.value
        }
        return sortedBook
    }

    fun add(record: Record) {
        val last = recordBook.keys().toList().lastOrNull()
        if (last != null) {
            recordBook[last + 1] = record
        }
        else recordBook[0] = record
    }

    fun get(id: Long): Record? {
        return recordBook[id]
    }

    fun remove(id: Long): Record? {
        return recordBook.remove(id)
    }

    fun update(id: Long, record: Record) {
        recordBook[id] = record
    }
}