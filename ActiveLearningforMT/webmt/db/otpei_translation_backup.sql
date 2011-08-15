-- MySQL dump 10.11
--
-- Host: localhost    Database: translation
-- ------------------------------------------------------
-- Server version	5.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `adityakrg`
--

DROP TABLE IF EXISTS `adityakrg`;
CREATE TABLE `adityakrg` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adityakrg`
--

LOCK TABLES `adityakrg` WRITE;
/*!40000 ALTER TABLE `adityakrg` DISABLE KEYS */;
INSERT INTO `adityakrg` VALUES ('2009-06-15 00:30:09','my name is adi bava','merA  ke nAma se  bava  Axi yad ZapahalA  hE','merA  ke nAma se  bava  Axi yad ZapahalA  hE'),('2009-06-15 11:21:59','This project is working fine.','isa pariyojanA kA kAma kara rahA hE, wo ho sakawA hE.','isa pariyojanA kA kAma kara rahA hE, wo ho sakawA hE. '),('2009-06-15 11:26:26','This is working fine!','yaha kAma kara rahA hE, wo ho sakawA hE.','yaha achchaA kaama kara raha he.'),('2009-06-24 11:37:13','my name is aditya.','merA ke nAma se aditya hE.','merA nAma ke se aditya hE. ');
/*!40000 ALTER TABLE `adityakrg` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `aswani_dutt`
--

DROP TABLE IF EXISTS `aswani_dutt`;
CREATE TABLE `aswani_dutt` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `aswani_dutt`
--

LOCK TABLES `aswani_dutt` WRITE;
/*!40000 ALTER TABLE `aswani_dutt` DISABLE KEYS */;
INSERT INTO `aswani_dutt` VALUES ('2009-06-15 10:58:24','this is working fine.','yaha kAma kara rahA hE, wo ho sakawA hE.','yaha achchaA kAma kara rahA hE. '),('2009-06-15 10:59:45','this is working fine.','yaha kAma kara rahA hE, wo ho sakawA hE.','yaha achchaA kama raha he.'),('2009-06-15 11:17:29','this project is working fine.','isa pariyojanA kA kAma kara rahA hE, wo ho sakawA hE.','isa pariyojanA kA kAma kara rahA hE, wo ho sakawA hE. '),('2009-06-15 11:23:00','this is working fine!','yaha kAma kara rahA hE, wo ho sakawA hE.','yaha achchaA kaAm kara raha he.');
/*!40000 ALTER TABLE `aswani_dutt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dutt`
--

DROP TABLE IF EXISTS `dutt`;
CREATE TABLE `dutt` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dutt`
--

LOCK TABLES `dutt` WRITE;
/*!40000 ALTER TABLE `dutt` DISABLE KEYS */;
INSERT INTO `dutt` VALUES ('2009-06-15 04:34:39','this is dutt','yahan dutt hai','yahan dutt hai'),('2009-06-15 04:44:23','my name is dutt','merA ke nAma se dutt hE.','merA nAma ke se dutt hE. '),('2009-06-15 04:47:20','This is Page1','yaha page1 hE.','yaha page12 hE. '),('2009-06-15 07:15:15','Language technology is often called human language technology ( HLT ) or natural language processing ( NLP ) and consists of computational linguistics ( or CL ) and speech technology as its core but includes also many application oriented aspects of them .','kahA jAwA hE  ki manuRZya   kI wakanIka BARA  ( wakanIkI BARA  hlt)   aWavA prAkqwika BARA ke rUpa meM unake prasaMskaraNa saMbaMXI   (nlp) Ora isameM  computational linguistics  (yA cl) Ora  BARaNa prOxyogikI meM  apanI mUla rUpa se BI SAmila hE, lekina oriented Avexanapawra ke pahalU hE.','kahA jAwA hE ki manuRZya kI wakanIka BARA ( wakanIkI BARA hlt) aWavA prAkqwika BARA ke rUpa meM unake prasaMskaraNa saMbaMXI (nlp) Ora isameM computational linguistics (yA cl) Ora BARaNa prOxyogikI meM apanI mUla rUpa se BI SAmila hE, lekina oriented Avexanapawra ke pahalU hE. '),('2009-06-15 07:17:17','This is Page1','yaha page1 hE.','asdf'),('2009-06-15 07:22:44','asd asd asd ad','asd asd asd vi&amp;Apana','qwe'),('2009-06-25 07:52:40','a das das as dad','eka xAsa dad ke rUpa meM xAsa','eka xAsa dad ke rUpa meM xAsa ');
/*!40000 ALTER TABLE `dutt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jitendar`
--

DROP TABLE IF EXISTS `jitendar`;
CREATE TABLE `jitendar` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `jitendar`
--

LOCK TABLES `jitendar` WRITE;
/*!40000 ALTER TABLE `jitendar` DISABLE KEYS */;
INSERT INTO `jitendar` VALUES ('2009-06-15 13:12:30','this is jitendar','yaha jitendar hE.','jitendar yaha ');
/*!40000 ALTER TABLE `jitendar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `muralik`
--

DROP TABLE IF EXISTS `muralik`;
CREATE TABLE `muralik` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `muralik`
--

LOCK TABLES `muralik` WRITE;
/*!40000 ALTER TABLE `muralik` DISABLE KEYS */;
/*!40000 ALTER TABLE `muralik` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ravi`
--

DROP TABLE IF EXISTS `ravi`;
CREATE TABLE `ravi` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ravi`
--

LOCK TABLES `ravi` WRITE;
/*!40000 ALTER TABLE `ravi` DISABLE KEYS */;
/*!40000 ALTER TABLE `ravi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test`
--

DROP TABLE IF EXISTS `test`;
CREATE TABLE `test` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `test`
--

LOCK TABLES `test` WRITE;
/*!40000 ALTER TABLE `test` DISABLE KEYS */;
INSERT INTO `test` VALUES ('2009-06-24 10:23:45','My name is Dutt.','merA ke nAma se dutt hE.','merA ke dutt hE. '),('2009-06-24 10:17:31','my name','merA nAma','merA nAma '),('2009-06-24 10:19:51','asd as das dasd ','asd ke rUpa meM dasd xAsa','asd ke rUpa meM dasd xAsa '),('2009-06-24 10:20:15','asd as dsa dasd ','asd dsa ke rUpa meM dasd','asd dsa ke rUpa meM dasd '),('2009-06-25 04:06:14','This is Page1','yaha hE. page12 ','yaha hE. page12 '),('2009-06-24 12:30:18','This is some text .','yaha kuCa kiyA jAwA hE.','mera text');
/*!40000 ALTER TABLE `test` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `username`
--

DROP TABLE IF EXISTS `username`;
CREATE TABLE `username` (
  `user` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  PRIMARY KEY  (`user`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `username`
--

LOCK TABLES `username` WRITE;
/*!40000 ALTER TABLE `username` DISABLE KEYS */;
INSERT INTO `username` VALUES ('adityakrg','iiit123'),('dutt','dutt'),('vamshi','asdf'),('vamshi_ambati','otpei'),('aswani_dutt','iiit123'),('muralik','murali'),('jitendar','jitendar'),('vambati','vamshi'),('test','test');
/*!40000 ALTER TABLE `username` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vambati`
--

DROP TABLE IF EXISTS `vambati`;
CREATE TABLE `vambati` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `vambati`
--

LOCK TABLES `vambati` WRITE;
/*!40000 ALTER TABLE `vambati` DISABLE KEYS */;
/*!40000 ALTER TABLE `vambati` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vamshi`
--

DROP TABLE IF EXISTS `vamshi`;
CREATE TABLE `vamshi` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `vamshi`
--

LOCK TABLES `vamshi` WRITE;
/*!40000 ALTER TABLE `vamshi` DISABLE KEYS */;
/*!40000 ALTER TABLE `vamshi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vamshi_ambati`
--

DROP TABLE IF EXISTS `vamshi_ambati`;
CREATE TABLE `vamshi_ambati` (
  `ts` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `English_Input` varchar(400) default NULL,
  `Moses_Hindi_Output` varchar(400) default NULL,
  `Final_Hindi_Output` varchar(400) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `vamshi_ambati`
--

LOCK TABLES `vamshi_ambati` WRITE;
/*!40000 ALTER TABLE `vamshi_ambati` DISABLE KEYS */;
INSERT INTO `vamshi_ambati` VALUES ('2009-06-15 08:41:41','the name of this project is Online Translation Post Editor Interface','isa pariyojanA kA nAma A vyavasWA edZItara paxa para anuvAxa hE.','isa pariyojanA kA nAma Online Translation Post-Editor Interface hE. '),('2009-06-15 08:47:42','Language technology is closely connected to computer science and general linguistics .','BARA hE ki prOxyogikI camadZE se ko  kaMpyUtara vijFAna Ora  janarala  linguistics  hE.',' BARA hE ki prOxyogikI camadZE se ko kaMpyUtara vijFAna Ora janarala linguistics hE.');
/*!40000 ALTER TABLE `vamshi_ambati` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-06-25  8:09:49
