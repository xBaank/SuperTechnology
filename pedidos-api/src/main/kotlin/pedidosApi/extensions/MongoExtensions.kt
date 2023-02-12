package pedidosApi.extensions

import org.bson.types.ObjectId

fun String.toObjectIdOrNull(): ObjectId? {
    if (!ObjectId.isValid(this))
        return null
    
    return ObjectId(this)
}