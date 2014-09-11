/*
 * Define msgType for different kinds of message
 */
package data;

public enum msgType {
	START,
	CONNECT,
	STARTFAIL,
	STARTDONE,
	FINISH,
	MIGRATENOTI,
	MIGRATEBEGIN,
	MIGRATEFAIL,
	MIGRATEDONE,
	HEART,
	HEARTACK,
	KILL,
	KILLFAIL,
	KILLDONE,
}