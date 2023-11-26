function openPopupForm() {
    document.getElementById("popupForm").style.display = "block";
}

function closePopupForm() {
    document.getElementById("popupForm").style.display = "none";
}

// JavaScript to toggle the sidebar
const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
const sidebar = document.querySelector('.sidebar');

document.addEventListener('DOMContentLoaded', function() {
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const sidebar = document.querySelector('.sidebar');

    sidebarToggleBtn.addEventListener('click', () => {
        sidebar.classList.toggle('active');
    })
});

const menuItems = document.querySelectorAll('.sidebar li a');

menuItems.forEach(item => {
    item.addEventListener('click', () => {
        // Удаляем класс 'active' у всех элементов меню
        menuItems.forEach(item => item.classList.remove('active'));

        // Добавляем класс 'active' к выбранному элементу меню
        item.classList.add('active');
    });
});

$(function () {
    $("#datepicker1").datepicker1({
        dateFormat: "yy-mm",
        changeMonth: true,
        changeYear: true,
        showButtonPanel: true,
        onClose: function (dateText, inst) {
            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).datepicker1('setDate', new Date(year, month, 2));

            $.get("/pc/" + dateText)
        }
    });
});

// Получаем текущую дату
var currentDate = new Date();

// Определяем дату, за которой доступен следующий месяц (текущая дата + 7 дней)
var nextMonthDate = new Date();
nextMonthDate.setDate(currentDate.getDate() + 7);

// Инициализация DatePicker
var datepicker = new Datepicker(document.querySelector('.datepicker'), {
    minDate: new Date(currentDate.getFullYear(), currentDate.getMonth() - 1), // Ограничение на предыдущий месяц
    maxDate: new Date(nextMonthDate.getFullYear(), nextMonthDate.getMonth()), // Ограничение на следующий месяц
    autohide: true, // Автоматическое скрытие календаря после выбора даты
    format: 'yyyy-mm-dd' // Формат даты
});