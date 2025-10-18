package ir.amirab.downloader.db

import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class MemoryDownloadListDB : IDownloadListDb {

    private val list: MutableList<IDownloadItem> = mutableListOf()
    override suspend fun getAll(): List<IDownloadItem> {
        return list.toList()
    }

    override suspend fun getById(id: Long): IDownloadItem? {
        return list.find { it.id == id }
    }

    override suspend fun add(item: IDownloadItem) {
        require(list.all { it.id != item.id }) {
            "duplicate download id"
        }
        list.add(item)
    }

    override suspend fun update(item: IDownloadItem) {
        list.indexOfFirst {
            it.id == item.id
        }.takeIf { it != -1 }?.let { index ->
            list.set(index, item)
        }
    }

    override suspend fun remove(item: IDownloadItem) {
        removeById(item.id)
    }

    override suspend fun removeById(itemId: Long) {
        val index = list.indexOfFirst {
            it.id == itemId
        }
        list.removeAt(index)
    }

    override suspend fun getLastId(): Long {
        return list.maxByOrNull {
            it.id
        }?.id ?: -1
    }

    private val flow = MutableSharedFlow<List<IDownloadItem>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    suspend fun onDbUpdate() {
        flow.tryEmit(getAll())
    }

    suspend fun allAsFlow() = flow
}
