#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <windows.h>
#include "serial_lib.h"
#include <time.h>
#define PORT "COM5"
#define MANUAL 1
#define WRITE_CMD 0x48
#define READ_CMD 0x52
#define ERROR_CMD 0x45
//#define MR_READ_CMD 0x40
#define BAUD 115200
#define TSLEEP 100
#define REPS 5
#define N 48
#define TESTUART 0
#define SINGLE 1

void abort(){
  printf("EXITING PROGRAM\n");
  close_serial();
  exit(0);
}
void fill(uint8_t *bfr, int data, int bytes){
  for(int i = 0; i < bytes; i++){
    bfr[i] = 0xff & (data >> (i*8));
  }
}
int bytes_to_int(uint8_t *bfr, int bytes, int reverse){
  int data = 0;
  for(int i = 0; i < bytes; i++){
    data = data + ((0xff & *(bfr+i)) << (reverse ? (bytes-1-i)*8 : i*8));
  }
  return data;
}
void read(uint8_t *bfr, int addr){
  fill(bfr+1, addr, 3);
  bfr[0] = READ_CMD;
  write_serial(bfr,4);
  Sleep(TSLEEP);
  read_serial(bfr,8);
  if(bfr[7] != 0x0A){
    printf("Missing new line on read\n");
  }else{
    printf("READ:  0x%08X, @ 0x%08X\n", bytes_to_int(bfr+3,4,0),bytes_to_int(bfr,3,0));
  }
}
void write(uint8_t *bfr, int addr, int data){
  bfr[0] = WRITE_CMD;
  fill(bfr+1, addr, 3);
  fill(bfr+4, data, 4);
  write_serial(bfr,8);
  Sleep(TSLEEP);
  if(bytes_serial(RX_BUFFER) != 8){
    printf("ERROR, only %d bytes in bfr\n",bytes_serial(RX_BUFFER));
  }
  read_serial(bfr,8);
  //addr = ((0xff & bfr[2]) << 16) + ((0xff & bfr[1]) << 8) + (0xff & bfr[0]);
  //data = ((0xff & bfr[6]) << 24) + ((0xff & bfr[5]) << 16) + ((0xff & bfr[4]) << 8) + (0xff & bfr[3]);
  if(bfr[7] != 0x0A){
    printf("Missing new line on write\n");
  }else{
    //printf("WRITE: 0x%08X, @ 0x%08X\n", bytes_to_int(bfr+3,4,0), bytes_to_int(bfr,3,0));
  }
}
/*
void read_whole(uint8_t *bfr){
  uint8_t mem[16];
  int addr = 0; 
  fill(bfr+1, addr, 3);
  bfr[0] = READ_CMD;
  write_serial(bfr,4);
  Sleep(TSLEEP);
  read_serial(bfr,8);
  if(bfr[7] != 0x0A){
    printf("Missing new line on read\n");
  }else{
    printf("READ:  0x%08X, @ 0x%08X\n", bytes_to_int(bfr+3,4,0),bytes_to_int(bfr,3,0));
  }
}
*/

int main(){
  uint8_t bfr[128];
  srand(time(NULL));
  if(!open_serial(PORT, BAUD)){
    printf("SUCCESS: Serial port now open for communication\n");
  }else{
    exit(1);
  }
  Sleep(500);
  char action = 'i';

  if(MANUAL){
    while(action != 'e'){
      action = getchar();
      if(action == 'w'){
        int data = 0;
        scanf("%x",&data);
        int addr = 0;
        scanf("%x",&addr);
        write(bfr, addr, data);
      }else if(action == 'r'){
        int data = 0;
        int addr = 0;
        scanf("%d",&addr);
        read(bfr, addr);
      }
    }
  }else if(TESTUART){
    for(int i = 0; i < REPS; i++){
      printf("\n------------------ ITERATION %d ----------------", i);
      printf("\nWROTE BYTES:\n");
      for(int j = 0; j < N; j++){
        bfr[j] = 0xff & rand();
        printf("%02X", bfr[j]);
        if((j != 0) && (j % 8 == 0)){
          printf("\n");
        }else{
          printf(" ");
        }
      }
      write_serial(bfr,N);
      for(int j = 0; j < N; j++){
        bfr[j] = 0;
      }
      Sleep(TSLEEP);
      printf("\nREAD %d BYTES:\n", read_serial(bfr,N));
      for(int j = 0; j < N; j++){
        printf("%02X", bfr[j]);
        if((j != 0) && (j % 8 == 0)){
          printf("\n");
        }else{
          printf(" ");
        }
      }
      printf("\n------------------------------------------------", i);
    }
  }else if(SINGLE){
    
    while(action != 'e'){
      action = getchar();
      if(action == 'w'){
        uint8_t data = 0;
        scanf("%x",&data);
        bfr[0] = data;
        write_serial(bfr,1);
      }else if(action == 'r'){
        printf("AVAIL: %d\n", bytes_serial(RX_BUFFER));
        read_serial(bfr,1);
        printf("%02X",bfr[0]);
        if(bfr[0] == 0x0A){
          printf("\n");
        }
      }
    }
  
   for(int i = 0; i < 40; i+=1){
     // Write valid = 1
     bfr[0] = 1;
     bfr[1] = 1;
     bfr[2] = i;
     bfr[3] = 0;
     bfr[4] = 0;
     bfr[5] = i;
     bfr[6] = 0;
     bfr[7] = 0;
     bfr[8] = 0;
     write_serial(bfr,9);
     Sleep(100);
     read_serial(bfr,1);
     if(bfr[0] != 0x0A){
      printf("Missing new line on write\n");
      return 1;
     }
     bfr[0] = 1;
     bfr[1] = 0;
     bfr[2] = i;
     bfr[3] = 0;
     bfr[4] = 0;
     bfr[5] = 0;
     bfr[6] = 0;
     bfr[7] = 0;
     bfr[8] = 0;
     write_serial(bfr,9);
     int b = read_serial(bfr,5);
     if(bfr[4] != 0x0A){
      printf("Missing new line on write\n");
      return 1;
     }
     printf("%02X %02X %02X %02X\n", bfr[3], bfr[2], bfr[1], bfr[0]);
   }
  }
  
  
  abort();
  return 0;
}