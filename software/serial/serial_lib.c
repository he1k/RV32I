#include <stdio.h>
#include <string.h>
#include <windows.h>
#include "serial_lib.h"

HANDLE hcom;

int open_serial(char * PATH, uint32_t BAUDRATE){

  hcom = CreateFileA(PATH,        //port name
                      GENERIC_READ | GENERIC_WRITE, //Read/Write
                      0,                            // No Sharing
                      NULL,                         // No Security
                      OPEN_EXISTING,                // Open existing port only
                      0,                            // Non Overlapped I/O
                      NULL);                        // Null for Comm Devices
  
  if(hcom == INVALID_HANDLE_VALUE){
    printf("Error %i from open: %s\n", errno, strerror(errno));
    return -1;
  }
  // Set r/w timeout to 300 ms
  COMMTIMEOUTS timeouts = {0};
  timeouts.ReadIntervalTimeout = 0;
  timeouts.ReadTotalTimeoutConstant = T_TIMEOUT;
  timeouts.ReadTotalTimeoutMultiplier = 0;
  timeouts.WriteTotalTimeoutConstant = T_TIMEOUT;
  timeouts.WriteTotalTimeoutMultiplier = 0;
  if (!SetCommTimeouts(hcom, &timeouts)){
    printf("Error: Couldn't set timeouts\n");
    return -1;
  }
 
  // Set the baud rate and other options.
  DCB state = {0};
  state.DCBlength = sizeof(DCB);
  state.BaudRate = BAUDRATE;
  state.ByteSize = 8;
  state.Parity = NOPARITY;
  state.StopBits = ONESTOPBIT;
  state.fNull = FALSE; // Don't discard null bytes
  state.fOutX = FALSE;
  state.fInX = FALSE;
  state.fBinary = TRUE;
  state.fDtrControl = DTR_CONTROL_ENABLE;
  state.fRtsControl = RTS_CONTROL_DISABLE;
  if (!SetCommState(hcom, &state)){
    printf("Error: Couldn't set port attributes\n");
    return -1;
  }
  /*
   DWORD mode;
  GetConsoleMode(hcom, &mode);
  mode &= ~ENABLE_LINE_INPUT;
  // And set the new mode
  SetConsoleMode(hcom, mode);
  */


  return 0;
}
int is_open(){
  if(hcom == INVALID_HANDLE_VALUE){
    printf("Error: Port is not open\n");
    return 0;
  }
  return 1;
}

unsigned long read_serial(uint8_t *bfr, size_t nbytes){
  if(!is_open()){
    return -1;
  }
  unsigned long n;
  if (!ReadFile(hcom, bfr, nbytes, &n, NULL)){
    printf("Error: Couldn't read from the port\n");
    return -1;
  }
	return n;
}
int write_serial(uint8_t *bfr, size_t nbytes){
  if(!is_open()){
    return -1;
  }

  unsigned long n;
  if (!WriteFile(hcom, bfr, nbytes, &n, NULL)){
    printf("Error: Couldn't write to the port\n");
    return -1;
  }
  //printf("\n\nWROTE %d BYTES \n\n", n);
  if (n != nbytes){
    printf("Error: Coulnd't write all bytes. Missing %lld bytes\n", nbytes-n);
    return -1;
  }
  return 0;
}
int close_serial(){
  if(!is_open()){
    return -1;
  }
  CloseHandle(hcom);
  return 0;
}
int flush_serial(){
  if(!is_open()){
    return -1;
  }
  if(!FlushFileBuffers(hcom)){
    printf("Failed to flush buffer\n");
    return -1;
  }
  return 0;
}
unsigned long bytes_serial(int dir){
  if(!is_open()){
    return -1;
  }
  COMSTAT com_stat;
  DWORD dw_errors;
  if(!ClearCommError(hcom, &dw_errors, &com_stat)){
    printf("Error: Couldn't access serial buffer status\n");
    return -1;
  }
  // Get error flag. Not in use right now, maybe in later versions
  /*
  BOOL fOOP, fOVERRUN, fPTO, fRXOVER, fRXPARITY, fTXFULL, fBREAK, fDNS, fFRAME, fIOE, fMODE;
  fDNS = dw_errors & CE_DNS;
  fIOE = dw_errors & CE_IOE;
  fOOP = dw_errors & CE_OOP;
  fPTO = dw_errors & CE_PTO;
  fMODE = dw_errors & CE_MODE;
  fBREAK = dw_errors & CE_BREAK;
  fFRAME = dw_errors & CE_FRAME;
  fRXOVER = dw_errors & CE_RXOVER;
  fTXFULL = dw_errors & CE_TXFULL;
  fOVERRUN = dw_errors & CE_OVERRUN;
  fRXPARITY = dw_errors & CE_RXPARITY;
  */

  if(dir == RX_BUFFER){
    return com_stat.cbInQue;
  }else if(dir == TX_BUFFER){
    return com_stat.cbOutQue;
  }else{
    printf("Error: Invalid argument\n");
    return -1;
  }
}