import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";


@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit{
  userId!: number;
  userDetail: any;

  constructor(private authService: AuthService) {
  }

  ngOnInit() {
    this.userId = this.authService.getUserId()!;
    this.getUserInfo();


  }

  getUserInfo() {
    this.authService.getUserInfo(this.userId).subscribe((data) => {
      this.userDetail = data;
    });
  }



  logout() {
    this.authService.logout();
  }


}
