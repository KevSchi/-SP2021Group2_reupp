const reservedDates = [];
console.log(campsite);

const days = [
  "Sonntag",
  "Montag",
  "Dienstag",
  "Mittwoch",
  "Donnerstag",
  "Freitag",
  "Samstag"
];

const months = [
  "Januar",
  "Februar",
  "März",
  "April",
  "Mai",
  "Juni",
  "Juli",
  "August",
  "September",
  "Oktober",
  "November",
  "Dezember"
];


const form = document.getElementById("form")
const date = new Date();
document.querySelector(".date p").innerHTML = `${days[date.getDay()]}, ${date.getDate()}. ${months[date.getMonth()]} ${date.getFullYear()}`;

const renderCalendar = () => {
  document.querySelector(".date h1").innerHTML = `${months[date.getMonth()]} <p>${date.getFullYear()}</p>`;
  const month = date.getMonth();
  const lastDay = new Date(date.getFullYear(), month + 1, 0);
  const firstDay = new Date(date.getFullYear(), month, 1);
  const prevLastDay = new Date(date.getFullYear(), month, 0);
  const nextDays = 7 - lastDay.getDay() + 1;
  const monthDays = document.querySelector(".days");

  let _days = "";

  if (firstDay.getDay() == 0) {
    for (let i = 6; i > 0; i--) {
      _days += `<div class="prev-date" data-date="${new Date(prevLastDay.getFullYear(), prevLastDay.getMonth(), prevLastDay.getDate() + 1 - i).toISOString()}">${prevLastDay.getDate() + 1 - i}</div>`;
    } //
  }
  for (let i = firstDay.getDay() - 1; i > 0; i--) {
    _days += `<div class="prev-date" data-date="${new Date(prevLastDay.getFullYear(), prevLastDay.getMonth(), prevLastDay.getDate() + 1 - i).toISOString()}">${prevLastDay.getDate() + 1 - i}</div>`;
  }

  for (let i = 1; i <= lastDay.getDate(); i++) {
    _days += `<div data-date="${new Date(date.getFullYear(), date.getMonth(), i).toISOString()}">${i}</div>`;
  }

  for (let i = 1; i < nextDays; i++) {
    _days += `<div class="next-date" data-date="${new Date(date.getFullYear(), date.getMonth() + 1, i).toISOString()}">${i}</div>`;
    monthDays.innerHTML = _days;
    if (i == nextDays - 1 && monthDays.querySelectorAll("div").length == 35) {
      for (let j = 1; j <= 7; j++) {
        _days += `<div class="next-date" data-date="${new Date(date.getFullYear(), date.getMonth() + 1, i + j).toISOString()}">${i + j}</div>`;
      }
    }
  }

  monthDays.innerHTML = _days;

  monthDays.querySelectorAll("div")
    .forEach(elem => {
      elem.addEventListener("click", e => {
        selectDate(e.target);
      })
      const date = new Date(elem.dataset.date);
      if (date.getDate() == new Date().getDate() && date.getMonth() == new Date().getMonth() && date.getFullYear() == new Date().getFullYear()) {
        elem.classList.add("today");
      }
    })

  checkReservedDates();

  if (isValid && startDate & endDate) {
    document.querySelector(".days").querySelectorAll("div")
      .forEach(div => {
        let date = new Date(div.dataset.date)
        if ((date.getTime() <= endDate.getTime() && date.getTime() >= startDate.getTime()))
          div.classList.add("selected-date");
      })
  }
}

function dateDiff(firstDate, secondDate) {
  return Math.round((secondDate - firstDate) / (1000 * 60 * 60 * 24));
}

document.querySelector(".previous")
  .addEventListener("click", () => {
    if (date.getMonth() == 0) {
      date.setFullYear(date.getFullYear() - 1);
      date.setMonth(11);
    } else {
      date.setMonth(date.getMonth() - 1);
    }
    renderCalendar();
  });

document.querySelector(".next")
  .addEventListener("click", () => {
    if (date.getMonth() == 11) {
      date.setFullYear(date.getFullYear() + 1);
      date.setMonth(0);
    } else {
      date.setMonth(date.getMonth() + 1);
    }
    renderCalendar();
  });


var startDate = null;
var endDate = null;
var isValid = true;

const selectDate = (element) => {
  if (element.classList.contains("reserved-date")) return;
  let date = new Date(element.dataset.date)
  if (!startDate) {
    startDate = date;
    element.classList.add("start-date");
    form.querySelector("input[name='start']").value = `${startDate.getFullYear()}-${(startDate.getMonth() + 1 < 10) ? "0" + (startDate.getMonth() + 1) : startDate.getMonth() + 1}-${(startDate.getDate() < 10) ? "0" + startDate.getDate() : startDate.getDate()}`;
  } else if (!endDate) {
    endDate = date
    element.classList.add("end-date");
    form.querySelector("input[name='end']").value = `${endDate.getFullYear()}-${(endDate.getMonth() + 1 < 10) ? "0" + (endDate.getMonth() + 1) : endDate.getMonth() + 1}-${(endDate.getDate() < 10) ? "0" + endDate.getDate() : endDate.getDate()}`;
  } else {
    startDate = null;
    endDate = null;
    form.querySelector("input[name='start']").value = "";
    form.querySelector("input[name='end']").value = "";
    form.querySelector("input[name='totalDays']").value = 0;
    document.querySelector(".start-date")?.classList.remove("start-date");
    document.querySelector(".end-date")?.classList.remove("end-date");
    document.querySelectorAll(".selected-date").forEach(elem => {
      elem?.classList.remove("selected-date");
    })
    isValid = true;
    return selectDate(element);
  };

  if (startDate && endDate) {
    if (endDate < startDate) {
      const save = startDate;
      startDate = endDate;
      endDate = save;
      const startEl = document.querySelector(".start-date");
      const endEl = document.querySelector(".end-date");
      startEl?.classList.add("end-date");
      startEl?.classList.remove("start-date");
      endEl?.classList.add("start-date");
      endEl?.classList.remove("end-date");
      form.querySelector("input[name='start']").value = `${startDate.getFullYear()}-${(startDate.getMonth() + 1 < 10) ? "0" + (startDate.getMonth() + 1) : startDate.getMonth() + 1}-${(startDate.getDate() < 10) ? "0" + startDate.getDate() : startDate.getDate()}`;
      form.querySelector("input[name='end']").value = `${endDate.getFullYear()}-${(endDate.getMonth() + 1 < 10) ? "0" + (endDate.getMonth() + 1) : endDate.getMonth() + 1}-${(endDate.getDate() < 10) ? "0" + endDate.getDate() : endDate.getDate()}`;
    }

    campsite.forEach(_date => {
      var currentDate = startDate;
      while (currentDate.getTime() != endDate.getTime()) {

        if ((currentDate.getTime() == new Date(_date.split('-')).getTime())) {
          isValid = false;
          break;
        }
        currentDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() + 1);
      }
    })

    if (!isValid) {
      startDate = null;
      endDate = null;
      form.querySelector("input[name='start']").value = "";
      form.querySelector("input[name='end']").value = "";
      form.querySelector("input[name='totalDays']").value = 0;
      isValid = true;
      const startEl = document.querySelector(".start-date");
      const endEl = document.querySelector(".end-date");
      startEl?.classList.remove("start-date");
      endEl?.classList.remove("end-date");
      var alertContent = "Der ausgewählte Zeitraum enthält schon reservierte Tage, bitte wählen Sie einen Zeitraum, der noch frei ist!";
      // Built-in function
      halfmoon.initStickyAlert({
        content: alertContent,      // Required, main content of the alert, type: string (can contain HTML)
        title: "Zeitraum ungültig",      // Optional, title of the alert, default: "", type: string
        alertType: "alert-danger"
      })
    } else {
      document.querySelector(".days").querySelectorAll("div")
        .forEach(div => {
          let date = new Date(div.dataset.date)
          if ((date.getTime() <= endDate.getTime() && date.getTime() >= startDate.getTime()))
            div.classList.add("selected-date");
        })

      form.querySelector("input[name='totalDays']").value = dateDiff(startDate, endDate) + 1;
      var alertContent = "Der ausgewählte Zeitraum ist noch zu haben, um ihn zu reservieren betätigen Sie den 'Bestätigen'-Knopf!";
      // Built-in function
      halfmoon.initStickyAlert({
        content: alertContent,      // Required, main content of the alert, type: string (can contain HTML)
        title: "Zeitraum verfügbar",      // Optional, title of the alert, default: "", type: string
        alertType: "alert-success"
      })

      document.querySelector("#startDate").value = form.querySelector("input[name='start']").value;
      document.querySelector("#endDate").value = form.querySelector("input[name='end']").value;
    }
  }
}

const checkReservedDates = () => {
  const dates = [];
  document.querySelector(".days").querySelectorAll("div").forEach(div => {
    dates.push(new Date(div.dataset.date).getTime());
  });
  campsite.forEach(_date => {
    const index = dates.indexOf(new Date(_date.split('-')).getTime());
    if (index != -1)
      document.querySelector(".days").querySelectorAll("div")[index].classList.add("reserved-date");
  })
}

const doForDifferntMonths = (startDate, endDate) => {
  var isValid = true;
  campsite.forEach((_date) => {
    var date = new Date(_date.split('-')).getTime();
    if ((date.getTime() <= endDate.getTime() && date.getTime() >= startDate.getTime())) {
      isValid = false;
    }
  })
  if (!isValid) {
    startDate = null;
    endDate = null;
    form.querySelector("input[name='start']").value = "";
    form.querySelector("input[name='end']").value = "";
    form.querySelector("input[name='totalDays']").value = 0;

    var alertContent = "Der ausgewählte Zeitraum enthält ein reservierten Zeitraum und wird deswegen zurückgesetzt!";
    // Built-in function
    halfmoon.initStickyAlert({
      content: alertContent,      // Required, main content of the alert, type: string (can contain HTML)
      title: "Zeitraum ungültig",      // Optional, title of the alert, default: "", type: string
      alertType: "alert-danger"
    })
    return;
  };

  const lastMonth = new Date(date.getFullYear(), date.getMonth(), 0);
  const nextDays = 7 - date.getDay();
  const lastDayOfPreviousMonth = lastMonth.getDate();
  var lastdays = lastDayOfPreviousMonth - startDate.getDate();
  console.log(lastDayOfPreviousMonth, nextDays, lastdays);
  for (let i = nextDays - 1; i >= 0; i--) {
    console.log(i);
    if (lastdays >= i) {
      document.querySelector(".days").querySelectorAll("div")[nextDays - i - 1]
        .classList.add("selected-date");
      lastdays--;
    }
  }
  for (let i = 0; i < endDate.getDate() - 1; i++) {
    document.querySelector(".days").querySelectorAll("div")[nextDays + i]
      .classList.add("selected-date");
  }
}

renderCalendar();