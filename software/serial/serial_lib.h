#include <stdint.h>
#define T_TIMEOUT 500 // Timeout in ms
#define RX_BUFFER 1
#define TX_BUFFER 0

int open_serial(char * PATH, uint32_t BAUDRATE);
unsigned long read_serial(uint8_t *bfr, size_t nbytes);
int write_serial(uint8_t *bfr, size_t nbytes);
int close_serial();
int flush_serial();
unsigned long bytes_serial(int dir);