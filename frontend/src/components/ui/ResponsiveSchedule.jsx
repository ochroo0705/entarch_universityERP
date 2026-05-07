import { useMemo, useState } from 'react';
import SelectMenu from './SelectMenu';

const SLOT_COLORS = [
  '#4f46e5', '#0891b2', '#7c3aed', '#059669', '#d97706',
  '#dc2626', '#2563eb', '#9333ea', '#ca8a04', '#0d9488',
  '#be185d', '#6d28d9', '#ea580c', '#0369a1', '#4338ca',
];

function getSubjectColor(subject, colorMap) {
  if (!colorMap.has(subject)) {
    colorMap.set(subject, SLOT_COLORS[colorMap.size % SLOT_COLORS.length]);
  }
  return colorMap.get(subject);
}

export default function ResponsiveSchedule({ schedule, dayNames, periodLabel, emptyLabel = '-', renderMeta }) {
  const [selectedDay, setSelectedDay] = useState(() => {
    const currentDay = new Date().getDay();
    return currentDay === 0 ? 1 : currentDay;
  });

  const { periods, days, lookup, periodTimes, colorMap, mobileAgenda } = useMemo(() => {
    const nextPeriods = [...new Set(schedule.map((item) => item.periodNumber))].sort((a, b) => a - b);
    const nextDays = [...new Set(schedule.map((item) => item.dayOfWeek))].sort((a, b) => a - b);
    const nextLookup = {};
    const nextPeriodTimes = {};

    schedule.forEach((item) => {
      nextLookup[`${item.dayOfWeek}-${item.periodNumber}`] = item;
      if (!nextPeriodTimes[item.periodNumber]) {
        nextPeriodTimes[item.periodNumber] = { start: item.startTime, end: item.endTime };
      }
    });

    return {
      periods: nextPeriods,
      days: nextDays,
      lookup: nextLookup,
      periodTimes: nextPeriodTimes,
      colorMap: new Map(),
      mobileAgenda: schedule.filter((item) => item.dayOfWeek === selectedDay).sort((a, b) => a.periodNumber - b.periodNumber),
    };
  }, [schedule, selectedDay]);

  const availableDay = days.includes(selectedDay) ? selectedDay : days[0];
  const mobileItems = availableDay === selectedDay
    ? mobileAgenda
    : schedule.filter((item) => item.dayOfWeek === availableDay).sort((a, b) => a.periodNumber - b.periodNumber);
  const dayOptions = days.map((day) => ({
    value: String(day),
    label: dayNames[day] || `Day ${day}`,
  }));

  return (
    <div className="responsive-schedule">
      <div className="schedule-table-wrap">
        <table className="schedule-grid">
          <thead>
            <tr>
              <th style={{ width: '110px', textAlign: 'center' }}>{periodLabel}</th>
              {days.map((day) => (
                <th key={day} style={{ textAlign: 'center', minWidth: '150px' }}>
                  {dayNames[day] || `Day ${day}`}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {periods.map((period) => {
              const time = periodTimes[period];
              return (
                <tr key={period}>
                  <td className="schedule-period-cell">
                    <div>P{period}</div>
                    {time ? <div className="schedule-period-time">{time.start?.slice(0, 5)} - {time.end?.slice(0, 5)}</div> : null}
                  </td>
                  {days.map((day) => {
                    const slot = lookup[`${day}-${period}`];
                    if (!slot) return <td key={day} className="schedule-empty-cell">{emptyLabel}</td>;

                    const bg = getSubjectColor(slot.subject, colorMap);
                    return (
                      <td key={day} style={{ padding: 0 }}>
                        <div className="schedule-slot-card" style={{ '--slot-color': bg }}>
                          <div className="schedule-slot-title">{slot.subject}</div>
                          <div className="schedule-slot-meta">{renderMeta(slot)}</div>
                        </div>
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="schedule-mobile">
        <div className="schedule-mobile-select">
          <SelectMenu
            options={dayOptions}
            value={String(availableDay || selectedDay || '')}
            onChange={(value) => setSelectedDay(Number(value))}
            placeholder="Select day"
          />
        </div>
        <div className="schedule-agenda">
          {mobileItems.length ? mobileItems.map((item) => (
            <article key={`${item.dayOfWeek}-${item.periodNumber}-${item.scheduleId || item.subject}`} className="schedule-agenda-card">
              <div className="schedule-agenda-time">
                <span>P{item.periodNumber}</span>
                <strong>{item.startTime?.slice(0, 5)} - {item.endTime?.slice(0, 5)}</strong>
              </div>
              <div className="schedule-agenda-body">
                <h3>{item.subject}</h3>
                <p>{renderMeta(item)}</p>
              </div>
            </article>
          )) : (
            <div className="state-panel state-panel-inline">
              <div className="empty-state">
                <h3>{emptyLabel}</h3>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
