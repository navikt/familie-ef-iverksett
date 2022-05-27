package no.nav.familie.ef.iverksett.repository

interface InsertUpdateRepository<T : Any> {

    fun insert(t: T): T
    fun insertAll(list: List<T>): List<T>

    fun update(t: T): T
    fun updateAll(list: List<T>): List<T>
}
