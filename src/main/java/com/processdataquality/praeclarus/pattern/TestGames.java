/*
 * Copyright (c) 2021-2022 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.pattern;

import java.util.ArrayList;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.gameelements.MCQuestion;

import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Test Games", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "This is a test plugin to replace the games")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class TestGames extends AbstractImperfectLabelContextual {

	//This line is added to test
	ArrayList<MCQuestion> questionBank;

	@Override
	protected void detect(StringColumn column, String s1, String s2) {
	}

	@Override
	public Options getOptions() {
		Options options = super.getOptions();
		// TODO define limit for each option
		return options;
	}

	@Override
	protected void detect(Table table, StringColumn selectedColumn, String sortColName) throws InvalidOptionException {
		_detected = table;
//		parser = new ParseTable(table, selectedColumn.name(), sortColName);
//		parser.parse();
		Table questions = getAuxiliaryDatasets().getTable("Questions");
		getAuxiliaryDatasets().put("Users", createUsersTable());
		getAuxiliaryDatasets().put("Answers1", createAnswersTable1());
		getAuxiliaryDatasets().put("Answers2", createAnswersTable2());
		getAuxiliaryDatasets().put("Answers3", createAnswersTable3());

	}

	protected Table createUsersTable() {
		Table userTable = Table.create("Users").addColumns(StringColumn.create("Username"), StringColumn.create("Role"),
				StringColumn.create("XPLevel"), StringColumn.create("KNLevel"), IntColumn.create("XPScore"),
				IntColumn.create("KNScore"), StringColumn.create("XPScoreHistory"),
				StringColumn.create("XPOverallScoreHistory"), StringColumn.create("KNScoreHistory"),
				StringColumn.create("KNOverallScoreHistory"), IntColumn.create("NextSynonym"),
				BooleanColumn.create("IsSurveyDone"), IntColumn.create("NextFact"), IntColumn.create("CurrentQID"),
				IntColumn.create("NextCharacter"));

		userTable.stringColumn(0).append("u1").append("u2");
		userTable.stringColumn(1).append("Nothing").append("Some how");
		userTable.stringColumn(2).append("Phenom Contributor").append("Good Starter");
		userTable.stringColumn(3).append("Expert").append("Beginner");
		userTable.intColumn(4).append(90).append(90);
		userTable.intColumn(5).append(65).append(65);
		userTable.stringColumn(6).append("[10,10,5]").append("[10,10,5]");
		userTable.stringColumn(7).append("[10,10,5]").append("[10,10,5]");
		userTable.stringColumn(8).append("[10,10,5]").append("[10,10,5]");
		userTable.stringColumn(9).append("[10,10,5]").append("[10,10,5]");
		userTable.intColumn(10).append(3).append(3);
		userTable.booleanColumn(11).append(true).append(true);
		userTable.intColumn(12).append(7).append(7);
		userTable.intColumn(13).append(8).append(8);
		userTable.intColumn(14).append(8).append(8);

		return userTable;
	}

	protected Table createAnswersTable1() {
		Table answersTable = Table.create("Answers1").addColumns(StringColumn.create("Username"),
				IntColumn.create("QID"), StringColumn.create("Detections"), StringColumn.create("Repair"));
		answersTable.stringColumn(0).append("u1").append("u1").append("u1").append("u2").append("u2").append("u2");
		answersTable.intColumn(1).append(0).append(1).append(2).append(0).append(1).append(2);
		answersTable.stringColumn(2).append("[9];[12];[13];[10]").append("[]").append("[1];[3];[2];[7]")
				.append("[9];[12];[13]").append("[]").append("[1];[3];[2]");
		answersTable.stringColumn(3).append("time-out").append("").append("get review").append("time-out").append("")
				.append("get review");
		return answersTable;
	}

	protected Table createAnswersTable2() {
		Table answersTable = Table.create("Answers2").addColumns(StringColumn.create("Username"),
				StringColumn.create("Detections"), StringColumn.create("Repair"), IntColumn.create("Topic"),
				IntColumn.create("Level"));
		answersTable.stringColumn(0).append("u1").append("u1").append("u1").append("u2").append("u2").append("u2");
		answersTable.stringColumn(1).append("[invite reviewers];[invite additional reviewers]")
				.append("[get review 2];[get review 3];[get review 1];[get review X]")
				.append("[time-out 1];[time-out 2];[time-out 3];[time-out X]")
				.append("[invite reviewers];[invite additional reviewers]")
				.append("[get review 2];[get review 3];[get review 1]")
				.append("[time-out 1];[time-out 2];[time-out 3]");
		answersTable.stringColumn(2).append("invite reviewers").append("get review").append("time-out")
				.append("invite reviewers").append("get review").append("time-out");
		answersTable.intColumn(3).append(1).append(1).append(1).append(1).append(1).append(1);
		answersTable.intColumn(4).append(1).append(1).append(2).append(1).append(1).append(2);
		return answersTable;
	}

	protected Table createAnswersTable3() {
		Table answersTable = Table.create("Answers3").addColumns(StringColumn.create("Username"),
				IntColumn.create("Label1"), IntColumn.create("Label2"), IntColumn.create("Relation"),
				StringColumn.create("Extra ID"), StringColumn.create("Extra Label"), IntColumn.create("Topic"),
				IntColumn.create("Level"));
		answersTable.stringColumn(0).append("u1").append("u1").append("u1").append("u1").append("u1").append("u1")
				.append("u1").append("u1").append("u1").append("u1").append("u2").append("u2").append("u2").append("u2")
				.append("u2").append("u2").append("u2").append("u2").append("u2").append("u2");
		answersTable.intColumn(1).append(8).append(9).append(9).append(6).append(1).append(1).append(1).append(9)
				.append(0).append(0).append(8).append(9).append(9).append(6).append(1).append(1).append(1).append(9)
				.append(0).append(0);
		answersTable.intColumn(2).append(11).append(12).append(13).append(11).append(3).append(2).append(7).append(10)
				.append(4).append(11).append(11).append(12).append(13).append(11).append(3).append(2).append(7).append(10)
				.append(4).append(11);
		answersTable.intColumn(3).append(2).append(1).append(1).append(0).append(1).append(1).append(1).append(1)
				.append(0).append(0).append(2).append(1).append(1).append(0).append(1).append(1).append(1).append(1)
				.append(0).append(0);
		answersTable.stringColumn(4).append("NULL").append("n0").append("n0").append("NULL").append("n1").append("n1")
				.append("n1").append("n0").append("NULL").append("NULL").append("NULL").append("n0").append("n0").append("NULL").append("n1").append("n1")
				.append("n1").append("n0").append("NULL").append("NULL");
		answersTable.stringColumn(5).append("NULL").append("time-out").append("time-out").append("NULL")
				.append("get review").append("get review").append("get review").append("time-out").append("NULL")
				.append("NULL").append("NULL").append("time-out").append("time-out").append("NULL")
				.append("get review").append("get review").append("get review").append("time-out").append("NULL")
				.append("NULL");
		answersTable.intColumn(6).append(1).append(1).append(1).append(1).append(1).append(1).append(1).append(1)
				.append(1).append(1).append(1).append(1).append(1).append(1).append(1).append(1).append(1).append(1)
				.append(1).append(1);
		answersTable.intColumn(7).append(1).append(1).append(1).append(1).append(2).append(2).append(2).append(1)
				.append(1).append(1).append(1).append(1).append(1).append(1).append(2).append(2).append(2).append(1)
				.append(1).append(1);
		return answersTable;
	}

	@Override
	public boolean canRepair() {
		return true;
	}

}
