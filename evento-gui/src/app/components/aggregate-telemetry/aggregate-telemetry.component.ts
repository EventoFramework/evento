import {Component, Input, OnInit} from '@angular/core';
import {PerformanceService} from "../../services/performance.service";
import {retry} from "rxjs";

@Component({
  selector: 'app-aggregate-telemetry',
  templateUrl: './aggregate-telemetry.component.html',
  styleUrls: ['./aggregate-telemetry.component.scss'],
})
export class AggregateTelemetryComponent implements OnInit {
  @Input() component!: any;

  chartOptions: any = {
    series: [
      {
        name: "My-series",
        data: [10, 41, 35, 51, 49, 62, 69, 91, 148]
      }
    ],
    chart: {
      height: 350,
      type: "bar"
    },
    title: {
      text: "My First Angular Chart"
    },
    xaxis: {
      categories: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep"]
    }
  };
  serviceTimeAggregation: 'AVG' | 'MAX' | 'MIN' = "AVG";
  datetimeFrom: string;
  datetimeTo: string;
  interval: number = 15*60;

  charts = []


  constructor(private service: PerformanceService) {
  }


  formatDateToCustomString(date) {
    function padZero(value) {
      return value.toString().padStart(2, '0');
    }

    let year = date.getFullYear();
    let month = padZero(date.getMonth() + 1); // Months are zero-based
    let day = padZero(date.getDate());
    let hours = padZero(date.getHours());
    let minutes = padZero(date.getMinutes());

    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  ngOnInit() {
    let d = new Date(new Date().getTime() - (4 * 60 * 60 * 1000));
    this.datetimeFrom = this.formatDateToCustomString(d);
    d = new Date();
    this.datetimeTo = this.formatDateToCustomString(d);
    return this.fetchTs()
  }

  async fetchTs() {
    const data = await this.service.getAggregatePerformanceTs(
      this.component.bundleId,
      this.component.componentName,
      new Date(this.datetimeFrom).toISOString(),
      new Date(this.datetimeTo).toISOString(),
      this.interval,
      this.serviceTimeAggregation
    )
    const charts = [];
    for (let handler of Object.keys(data)) {
      charts.push({
        id: handler,
        series: [
          {
            name: "Count",
            type: "column",
            data: data[handler].map((e: any) => ({x:new Date(e.timestamp).getTime(), y:parseInt(e.count)}))
          },
          {
            name: "Lock Time",
            type: "column",
            color: "#C70039",
            data: data[handler].map((e: any) => ({x:new Date(e.timestamp).getTime(), y:e.lock?.toFixed(2) || null}))
          },
          {
            name: "Retrieve Time",
            type: "column",
            color: "blue",
            data: data[handler].map((e: any) => ({x:new Date(e.timestamp).getTime(), y:e.retrieve?.toFixed(2) || null}))
          },
          {
            name: "Service Time",
            type: "column",
            color: "#23F622",
            data: data[handler].map((e: any) => ({x:new Date(e.timestamp).getTime(), y:e.serviceTime?.toFixed(2) || null}))
          },
          {
            name: "Store Time",
            type: "column",
            color: "#FC9F29",
            data: data[handler].map((e: any) => ({x:new Date(e.timestamp).getTime(), y:e.store?.toFixed(2) || null}))
          }

          /*


         */
        ],
        chart: {
          height: 350,
          type: "line",
          stacked: true,
          id: handler,
          group: this.component.componentName
        },
        dataLabels: {
          enabled: false
        },
        title: {
          text: handler,
          align: "left",
          offsetX: 0
        },
        xaxis: {
          type: 'datetime',
          labels: {
            datetimeUTC: false
          },
          fill: {
            opacity: 1
          }
        },
        yaxis:[
          {
            seriesName: "Count",
            axisTicks: {
              show: true
            },
            axisBorder: {
              show: true,
              color: "#008FFB"
            },
            labels: {
              style: {
                color: "#008FFB"
              }
            },
            title: {
              text: "Handled Count",
              style: {
                color: "#008FFB"
              }
            },
            tooltip: {
              enabled: true
            },
          },
          {
            opposite: true,
            axisTicks: {
              show: true
            },
            axisBorder: {
              show: true,
              color: "#FEB019"
            },
            labels: {
              style: {
                color: "#FEB019"
              }
            },
            title: {
              text: "Service Time",
              style: {
                color: "#FEB019"
              }
            }
          },
        ]
      })
    }
    this.charts = charts;
  }

  tsFormChanged() {
    this.fetchTs();
  }
}
