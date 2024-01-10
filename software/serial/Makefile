CC=gcc
CFLAGS=#-Wall
LDFLAGS=-lraylib -lm
OBJFILES= main.o serial_lib.o
TARGET=run
all: $(TARGET)

$(TARGET) : $(OBJFILES)
	$(CC) $(CFLAGS) -o  $(TARGET).exe $(OBJFILES) $(LDFLAGS)
	./$(TARGET)

clean:
	-del -fR $(OBJFILES) $(TARGET).exe
