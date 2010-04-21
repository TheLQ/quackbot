-- phpMyAdmin SQL Dump
-- version 3.2.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 18, 2010 at 04:54 PM
-- Server version: 5.1.41
-- PHP Version: 5.3.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `quackbot`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
CREATE TABLE IF NOT EXISTS `admin` (
  `ADMIN_ID` int(5) NOT NULL AUTO_INCREMENT,
  `SERVER_ID` int(5) DEFAULT NULL,
  `CHANNEL_ID` varchar(5) DEFAULT NULL,
  `user` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ADMIN_ID`),
  KEY `SERVER_ID` (`SERVER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- RELATIONS FOR TABLE `admin`:
--   `SERVER_ID`
--       `server` -> `SERVER_ID`
--

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`ADMIN_ID`, `SERVER_ID`, `CHANNEL_ID`, `user`) VALUES
(1, 1, '0', 'LordQuackstar');

-- --------------------------------------------------------

--
-- Table structure for table `channel`
--

DROP TABLE IF EXISTS `channel`;
CREATE TABLE IF NOT EXISTS `channel` (
  `CHANNEL_ID` int(5) NOT NULL AUTO_INCREMENT,
  `SERVER_ID` int(5) DEFAULT NULL,
  `channel` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`CHANNEL_ID`),
  KEY `SERVER_ID` (`SERVER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- RELATIONS FOR TABLE `channel`:
--   `SERVER_ID`
--       `server` -> `SERVER_ID`
--

--
-- Dumping data for table `channel`
--

INSERT INTO `channel` (`CHANNEL_ID`, `SERVER_ID`, `channel`, `password`) VALUES
(1, 1, '#quackbot', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
CREATE TABLE IF NOT EXISTS `contacts` (
  `CONTACT_ID` varchar(40) NOT NULL,
  `PASSWORD` varchar(40) NOT NULL,
  `FIRST_NAME` varchar(40) NOT NULL,
  `LAST_NAME` varchar(60) NOT NULL,
  `COMPANY_NAME` varchar(60) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `CREATED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CONTACT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `contacts`
--


-- --------------------------------------------------------

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
CREATE TABLE IF NOT EXISTS `log` (
  `LOG_ID` int(5) NOT NULL AUTO_INCREMENT,
  `SERVER_ID` varchar(5) DEFAULT NULL,
  `address` varchar(50) DEFAULT NULL,
  `timestamp` varchar(100) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `channel` varchar(50) DEFAULT NULL,
  `sender` varchar(50) DEFAULT NULL,
  `login` varchar(50) DEFAULT NULL,
  `hostname` varchar(75) DEFAULT NULL,
  `message` varchar(9000) DEFAULT NULL,
  `command` varchar(50) DEFAULT NULL,
  `args` varchar(9000) DEFAULT NULL,
  PRIMARY KEY (`LOG_ID`),
  KEY `SERVER_ID` (`SERVER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `log`
--


-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
CREATE TABLE IF NOT EXISTS `orders` (
  `ORDER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTACT_ID` varchar(40) NOT NULL,
  `PRODUCT` varchar(40) NOT NULL,
  `QUANTITY` int(11) NOT NULL,
  `PRICE` double NOT NULL,
  `STATUS` varchar(20) NOT NULL DEFAULT 'unverified',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ORDER_ID`),
  KEY `CONTACT_ID` (`CONTACT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- RELATIONS FOR TABLE `orders`:
--   `CONTACT_ID`
--       `contacts` -> `CONTACT_ID`
--

--
-- Dumping data for table `orders`
--


-- --------------------------------------------------------

--
-- Table structure for table `server`
--

DROP TABLE IF EXISTS `server`;
CREATE TABLE IF NOT EXISTS `server` (
  `SERVER_ID` int(5) NOT NULL AUTO_INCREMENT,
  `address` varchar(50) DEFAULT NULL,
  `port` varchar(5) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`SERVER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `server`
--

INSERT INTO `server` (`SERVER_ID`, `address`, `port`, `password`) VALUES
(1, 'irc.freenode.net', '8000', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `support`
--

DROP TABLE IF EXISTS `support`;
CREATE TABLE IF NOT EXISTS `support` (
  `SUPPORT_ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTACT_ID` varchar(40) NOT NULL,
  `CODE` varchar(10) NOT NULL,
  `STATUS` varchar(20) NOT NULL,
  `PHONE` varchar(20) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `REQUEST` varchar(255) NOT NULL,
  `CREATED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`SUPPORT_ID`),
  KEY `CONTACT_ID` (`CONTACT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- RELATIONS FOR TABLE `support`:
--   `CONTACT_ID`
--       `contacts` -> `CONTACT_ID`
--

--
-- Dumping data for table `support`
--


--
-- Constraints for dumped tables
--

--
-- Constraints for table `admin`
--
ALTER TABLE `admin`
  ADD CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`SERVER_ID`) REFERENCES `server` (`SERVER_ID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `channel`
--
ALTER TABLE `channel`
  ADD CONSTRAINT `channel_ibfk_1` FOREIGN KEY (`SERVER_ID`) REFERENCES `server` (`SERVER_ID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`CONTACT_ID`) REFERENCES `contacts` (`CONTACT_ID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `support`
--
ALTER TABLE `support`
  ADD CONSTRAINT `support_ibfk_1` FOREIGN KEY (`CONTACT_ID`) REFERENCES `contacts` (`CONTACT_ID`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
