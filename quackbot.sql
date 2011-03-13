--
-- Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
--
-- This file is part of Quackbot.
--
-- Quackbot is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- Quackbot is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
--

-- phpMyAdmin SQL Dump
-- version 3.2.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 30, 2010 at 10:36 PM
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

CREATE TABLE IF NOT EXISTS `admin` (
  `ADMIN_ID` int(5) NOT NULL AUTO_INCREMENT,
  `SERVER_ID` int(5) DEFAULT NULL,
  `CHANNEL_ID` int(5) DEFAULT NULL,
  `user` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ADMIN_ID`),
  KEY `SERVER_ID` (`SERVER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`ADMIN_ID`, `SERVER_ID`, `CHANNEL_ID`, `user`) VALUES
(1, 1, NULL, 'LordQuackstar');

-- --------------------------------------------------------

--
-- Table structure for table `channel`
--

CREATE TABLE IF NOT EXISTS `channel` (
  `CHANNEL_ID` int(5) NOT NULL AUTO_INCREMENT,
  `SERVER_ID` int(5) DEFAULT NULL,
  `channel` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`CHANNEL_ID`),
  KEY `SERVER_ID` (`SERVER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `channel`
--

INSERT INTO `channel` (`CHANNEL_ID`, `SERVER_ID`, `channel`, `password`) VALUES
(1, 1, '#quackbot', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `log`
--

CREATE TABLE IF NOT EXISTS `log` (
  `LOG_ID` int(5) NOT NULL AUTO_INCREMENT,
  `SERVER_ID` int(5) DEFAULT NULL,
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
-- Table structure for table `server`
--

CREATE TABLE IF NOT EXISTS `server` (
  `SERVER_ID` int(5) NOT NULL AUTO_INCREMENT,
  `address` varchar(50) DEFAULT NULL,
  `port` varchar(5) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`SERVER_ID`),
  KEY `SERVER_ID` (`SERVER_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `server`
--

INSERT INTO `server` (`SERVER_ID`, `address`, `port`, `password`) VALUES
(1, 'irc.freenode.net', '8000', NULL);

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
-- Constraints for table `log`
--
ALTER TABLE `log`
  ADD CONSTRAINT `log_ibfk_1` FOREIGN KEY (`SERVER_ID`) REFERENCES `server` (`SERVER_ID`) ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
