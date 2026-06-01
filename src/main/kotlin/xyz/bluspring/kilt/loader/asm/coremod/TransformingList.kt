package xyz.bluspring.kilt.loader.asm.coremod

class TransformingList<E>(
    val backing: MutableList<E>,
    val transformer: (E) -> E
) : MutableList<E> {
    override fun add(element: E): Boolean = this.backing.add(this.transformer(element))
    override fun remove(element: E): Boolean = this.backing.remove(element)
    override fun addAll(elements: Collection<E>): Boolean = this.backing.addAll(elements.map { this.transformer(it) })
    override fun addAll(index: Int, elements: Collection<E>): Boolean = this.backing.addAll(index, elements.map { this.transformer(it) })
    override fun removeAll(elements: Collection<E>): Boolean = this.backing.removeAll(elements)
    override fun retainAll(elements: Collection<E>): Boolean = this.backing.retainAll(elements)
    override fun clear() = this.backing.clear()
    override fun set(index: Int, element: E): E = this.backing.set(index, this.transformer(element))
    override fun add(index: Int, element: E) = this.backing.add(index, this.transformer(element))
    override fun removeAt(index: Int): E = this.backing.removeAt(index)
    override fun listIterator(): MutableListIterator<E> = this.backing.listIterator()
    override fun listIterator(index: Int): MutableListIterator<E> = this.backing.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = this.backing.subList(fromIndex, toIndex)
    override val size: Int
        get() = this.backing.size
    override fun isEmpty(): Boolean = this.backing.isEmpty()
    override fun contains(element: E): Boolean = this.backing.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = this.backing.containsAll(elements)
    override fun get(index: Int): E = this.backing[index]
    override fun indexOf(element: E): Int = this.backing.indexOf(element)
    override fun lastIndexOf(element: E): Int = this.backing.lastIndexOf(element)
    override fun iterator(): MutableIterator<E> = this.backing.iterator()
}
