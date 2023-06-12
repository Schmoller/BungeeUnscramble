CREATE TABLE `players` (
  `playerid` varchar(40) NOT NULL,
  `totalpoints` int(11) DEFAULT NULL,
  `points` int(11) DEFAULT NULL,
  `wins` int(11) DEFAULT NULL,
  PRIMARY KEY (`playerid`)
)
