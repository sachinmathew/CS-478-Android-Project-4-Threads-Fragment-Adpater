# CS-478-Android-Project-4-Threads-Fragment-Adpater

This project is about threads amd why it is important in android. Mobile unlike laptops or computers has only limited resources.
So we cannot run process which take long time in main UI thread. If UI is not responsive for some seconds OS will throw error. 
So it is typical to give time consuming process to run as background threads.

This project is a number guessing game by 2 threads and displaying thier guesses synchronously.This project we have 3 threads.
Main UI thread and 2 worker Threads. UI thread can only respond with UI. Each worker thread is a player. So game starts by each woker thread setting a 4 digit secret number. Then it will display
them in UI. 

Now first thread guess a number and send it handler of second thread. Second thread analyze the guess and give feedback to thread 1
as in which numbers are right. Then it makes it own guess and send it to player 1 and this process repeats. First player to
guess correctly wins. After 20 moves no on was able to find number we declare draw.
