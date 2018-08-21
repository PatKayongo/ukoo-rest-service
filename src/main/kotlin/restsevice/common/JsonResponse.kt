package restsevice.common

class JsonResponse<T>(t: T?, errors: List<String> = ArrayList<String>()) {
    val errors: List<String> = errors
    val data: T? = t
}