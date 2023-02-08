

//ej : fun LocalDate.toLocalDate(): String {
return this.format(
DateTimeFormatter
.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("es", "ES"))
)
}

fun Double.toLocalMoney(): String {
return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(this)
}

fun Double.toLocalNumber(): String {
return NumberFormat.getNumberInstance(Locale("es", "ES")).format(this)
}