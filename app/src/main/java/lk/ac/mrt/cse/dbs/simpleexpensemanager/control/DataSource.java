package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.lang.reflect.AccessibleObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;
// 200088D
public class DataSource extends SQLiteOpenHelper {
    private static final String dbName = "200088D";
    private static final int dbVersion = 1;

    private static final String tableNameTransaction = "transactionLog";
    private static final String idColTransaction = "id";
    private static final String dateColTransaction = "date";
    private static final String accNoColTransaction = "accNo";
    private static final String expenseTypeColTransaction = "expenseType";
    private static final String amountColTransaction = "amount";

    private static final String tableNameAccount = "account";
    private static final String accNoAccount = "accNo";
    private static final String bankNameAccount = "bankName";
    private static final String accountHolderNameAccount = "accountHolderName";
    private static final String balanceAccount = "balance";

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DataSource(Context context) {
        super(context, dbName, null, dbVersion);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String queryAccount = "create table " + tableNameAccount + " ("
                + accNoAccount + " text primary key,"
                + bankNameAccount + " text,"
                + accountHolderNameAccount + " text,"
                + balanceAccount + " double)";

        String queryTransaction = "create table " + tableNameTransaction + " ("
                + idColTransaction + " integer primary key autoincrement,"
                + dateColTransaction + " date,"
                + accNoColTransaction + " text,"
                + expenseTypeColTransaction + " text,"
                + amountColTransaction + " double,"
                + "foreign key (" + accNoColTransaction + ") references " + tableNameAccount + " )";

        sqLiteDatabase.execSQL(queryAccount);
        sqLiteDatabase.execSQL(queryTransaction);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + tableNameTransaction);
        sqLiteDatabase.execSQL("drop table if exists " + tableNameAccount);
        onCreate(sqLiteDatabase);
    }

    private String getStringFromData(Date date) {
        if (date == null){
            return null;
        }
        return dateFormat.format(date);
    }

    private Date getDateFromString(String string){
        try {
            return dateFormat.parse(string);
        }catch (ParseException | NullPointerException e){
            return null;
        }
    }

    public void addTransaction(Transaction transaction){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(dateColTransaction,getStringFromData(transaction.getDate()));
        contentValues.put(accNoColTransaction,transaction.getAccountNo());
        String expenseType = null;
        if (transaction.getExpenseType() == ExpenseType.EXPENSE){
            expenseType = "EXPENSE";
        }else {
            expenseType = "INCOME";
        }
        contentValues.put(expenseTypeColTransaction,expenseType);
        contentValues.put(amountColTransaction,transaction.getAmount());
        sqLiteDatabase.insert(tableNameTransaction,null,contentValues);
        sqLiteDatabase.close();
    }

    public void addAccount(Account account){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(accNoAccount,account.getAccountNo());
        contentValues.put(bankNameAccount,account.getBankName());
        contentValues.put(accountHolderNameAccount,account.getAccountHolderName());
        contentValues.put(balanceAccount,account.getBalance());
        sqLiteDatabase.insert(tableNameAccount,null,contentValues);
        sqLiteDatabase.close();
    }

    public List<Transaction> getTransactions(){
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "select * from " + tableNameTransaction;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()){
            do{
                Date date = getDateFromString(cursor.getString(1));
                String accNo = cursor.getString(2);
                ExpenseType expenseType = null;
                if (Objects.equals(cursor.getString(3), "EXPENSE")){
                    expenseType = ExpenseType.EXPENSE;
                }else{
                    expenseType = ExpenseType.INCOME;
                }
                Double amount = cursor.getDouble(4);
                Transaction transaction = new Transaction(date,accNo,expenseType,amount);
                transactions.add(transaction);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }

    public List<Account> getAccounts(){
        List<Account> accounts = new ArrayList<>();
        String selectQuery = "select * from " + tableNameAccount;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()){
            do{
                String accountNo = cursor.getString(0);
                String bankName = cursor.getString(1);
                String accountHolder = cursor.getString(2);
                Double balance = cursor.getDouble(3);
                Account account = new Account(accountNo,bankName,accountHolder,balance);
                accounts.add(account);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return accounts;
    }

    public void updateAccount(Account account){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(accNoAccount,account.getAccountNo());
        contentValues.put(bankNameAccount,account.getBankName());
        contentValues.put(accountHolderNameAccount,account.getAccountHolderName());
        contentValues.put(balanceAccount,account.getBalance());
        sqLiteDatabase.update(tableNameAccount,contentValues,accNoAccount + " =? ",new String[]{account.getAccountNo()});
        sqLiteDatabase.close();
    }

    public void deleteAccount(Account account){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(tableNameAccount, accNoAccount + " =? ", new String[]{account.getAccountNo()});
        sqLiteDatabase.close();
    }


}
