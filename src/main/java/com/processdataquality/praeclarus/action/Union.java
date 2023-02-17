/*
 * Copyright (c) 2021-2023 Queensland University of Technology
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

package com.processdataquality.praeclarus.action;

import java.util.List;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;

import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 21/5/21
 */
@Plugin(name = "Union", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Returns the union of a set of tables (they must have the same schema)")
public class Union extends AbstractAction {

	public Union() {
		super();
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		if (inputList.size() < 2) {
			throw new InvalidOptionValueException("This action requires at least two tables as input.");
		}
		if (!sameSchema(inputList)) {
			throw new InvalidOptionValueException("Input tables do not have the same schema");
		}

		Table t1 = inputList.remove(0);
		Table res = t1.copy();
		for (Table t : inputList) {
			res.append(t);
			System.out.println("Appended " + res.rowCount());

		}
		return res.dropDuplicateRows();

	}

	@Override
	public int getMaxInputs() {
		return 2;
	}

}
