
import warnings
warnings.filterwarnings(action='ignore')
import os
import pandas as pd
import numpy as np
import plotly.io as pio
import networkx as nx
import matplotlib.pyplot as plt

import matplotlib
matplotlib.use('Agg')

pio.renderers.default = 'iframe_connected'

my_path = os.path.abspath('')

from PIL import Image, ImageDraw
import random
from matplotlib.colors import ListedColormap
from collections import deque



# --- 병렬 처리를 위한 작업자 함수 ---
def process_case_id_wrapper(case_id_for_processing, # 첫 번째 인자는 Pool.map에서 오는 반복요소
                           # 이하 고정 인자들
                           p_anomaly_nolabel,
                           p_filtered_actset,
                           p_act_freq,
                           p_actset,
                           p_NBGs,
                           p_num_epi,
                           alpha):
    """
    단일 caseid에 대한 원래의 루프 내용을 처리하는 함수.
    multiprocessing.Pool의 워커에 의해 호출됩니다.
    """
    # 각 케이스별로 독립적인 QLearningAgent 인스턴스 생성
    # filtered_actset과 act_freq가 비어있지 않거나 QLearningAgent가 이를 처리할 수 있는지 확인 필요
    # 원본 코드에서는 루프 내에서 agent를 매번 생성했으므로 동일하게 유지
    agent = QLearningAgent(p_filtered_actset ,  p_act_freq,learning_rate=0.1, discount_factor=0.9, exploration_rate=0.5, exploration_decay_rate=0.0001)
    
    # print(f"Case ID: {case_id_for_processing}") # 병렬 처리 시 print는 섞일 수 있음
    obs_case = p_anomaly_nolabel.loc[p_anomaly_nolabel['case:concept:name']  == case_id_for_processing].reset_index(drop=True)

    env = P_BEAR_RL(obs_case, p_actset, p_filtered_actset, p_NBGs, alpha =alpha) # jh
    episodes = p_num_epi

    shortest_episode = float('inf') # 이 변수는 사용되지 않은 것 같습니다.
    highest_reward_shortest_episode = -float('inf') # 이 변수는 사용되지 않은 것 같습니다.
    best_episode_history = None # 이 변수는 사용되지 않은 것 같습니다.
    all_episode_histories = []

    for episode in range(episodes):
        state = env.reset()
        total_reward = 0
        steps = 0
        done = False
        game_over = False
        episode_history = []

        while not done and not game_over:
            action = agent.select_action(state)
            next_state, reward, done, game_over, info, loc, label_act, rework = env.step(action, max_step=8)

            agent.update_q_value(state, action, reward, next_state)
            agent.decay_exploration_rate()

            total_reward += reward
            steps += 1
            if not done and (label_act != None): #jh
                episode_history.append((state.copy(), env.action_labels[action].split('_')[0], loc, label_act, rework,reward, next_state.copy()))
            state = next_state

        if done and not game_over:
            adm_last = env.ADM(next_state, 0 ,p_NBGs )
            all_episode_histories.append((steps-1, np.sum(adm_last)/(len(adm_last)*len(adm_last[0])), episode_history))

            # all_episode_histories.append((steps-1, total_reward, episode_history))
            # env.render() # 병렬 처리 중 GUI 렌더링은 적합하지 않음

    if len(all_episode_histories)>0:
        check_clean = [h[0] for h in all_episode_histories]
        if max(check_clean)>0:
            # 최종 결과 선택
            
            shortest_episodes = [hist for hist in all_episode_histories if hist[0] == min(h[0] for h in all_episode_histories if h[0] > 0)] # 0 step 제외

            best_episode = min(shortest_episodes, key=lambda x: x[1])
            env.state = best_episode[2][-1][-1] # 마지막 상태 렌더링
            
            env.state['predict_patterns'] = discover_pattern(best_episode[2])
            case_result_df = env.state

            # env.render()
        else:
            state = env.init_state
            state['predict_patterns'] = ''
            case_result_df = state
    else:
        state = env.init_state
        state['predict_patterns'] = ''
        case_result_df = state

    return case_result_df


def discover_NBG(clean, vis = False):

    # 트레이스 및 변형 빈도 계산
    traces = clean.groupby('case:concept:name', observed=True)['concept:name'].transform(lambda x: '>>'.join(x))
    variant_freq = traces.value_counts().rename_axis('trace').reset_index(name='counts')
    variant_freq['var_id'] = [ 'var_' + str(i) for i in range(0,len(variant_freq))]
    variant_freq['element'] = variant_freq['trace'].apply(lambda x: list(x.split('>>'))) 


    # 그래프 생성을 위한 데이터 준비
    clean2 = clean[['case:concept:name', 'concept:name', 'order']].rename(columns={"concept:name": "from", "order": "level"}, copy=False)
    clean2['FROM'] = clean2['from'].astype(str) + '_' + clean2['level'].astype(str)

    # clean3 = clean2[clean2['from'] != 'End'].copy() # 'from' 컬럼 기준으로 'End' 제거
    # clean3['TO'] = clean3.groupby('case:concept:name', observed=True)['FROM'].shift(-1).fillna('End_inf') # 다음 액티비티를 'TO'로, 마지막은 'End_inf'로 처리
    # clean3 = clean3[clean3['TO'] != 'End_inf'].copy() # 마지막 'End_inf' 행 제거
    # clean3 = clean3[['case:concept:name', 'FROM', 'TO', 'level']].reset_index(drop=True)

    # del clean3 # 더 이상 필요없는 DataFrame 삭제

    NBGs = []
    actset = clean['concept:name'].unique()

    for i in range(0, len(actset)):
        # print(actset[i])
        cat_1 = variant_freq[variant_freq['element'].apply(lambda x: actset[i] in x)]
        cat_1['duplicate'] = cat_1['element'].apply(lambda x: sum( pd.Series(x).isin([actset[i]]) ) )
        cat_2 = pd.DataFrame( { 'var_id': np.repeat(cat_1['var_id'], cat_1['element'].apply(len)), 
                                'concept:name': sum( cat_1['element'], []), 
                                'counts': np.repeat(cat_1['counts'], cat_1['element'].apply(len))})
        cat_2['level'] = 0
        cat_2.loc[ cat_2['concept:name'] == actset[i], 'level' ] = 1
        cat_2a = cat_2.groupby('var_id')['level']
        cat_2['level'] = cat_2a.cumcount() - cat_2a.transform(lambda x: (x.values).argmax())

        # 한 seq에 같은 activity가 2개 이상 존재하는 경우
        if sum(cat_1['duplicate'] > 1 )>0:
            for index, row in cat_1[cat_1['duplicate'] > 1].iterrows(): 
                # 신규 variant 생성
                temp1 = cat_2[cat_2['var_id']== str(row['var_id'])]
                cat_3 = pd.concat([temp1]*(row['duplicate']-1))
                cat_3['var_id'] = np.repeat([ "{}_dup_{}".format(a, b) for a, b in zip( list(cat_3['var_id']), range(0, row['duplicate']-1 ) ) ], len(temp1))
                # base level 조정
                locs = np.where(temp1['concept:name'] == actset[i])
                temp2 = np.repeat(locs[0][1:], len(temp1))
                cat_3['level'] = list(range(0,len(temp1)))*(row['duplicate']-1) - temp2
                cat_2 = pd.concat( [cat_2, cat_3] )


        cat_3 = cat_2.rename(columns={"concept:name": "from"})
        cat_3['FROM'] = cat_3['from'] + '_' + cat_3['level'].astype(str)
        cat_3.reset_index(drop=True, inplace=True)
        cat_4 = cat_3.drop(index=cat_3.index[len(cat_3)-1])
        cat_4['TO'] = list(cat_3.drop(index=cat_3.index[0],)['FROM'])

        cat_4 = cat_4.drop( cat_4[cat_4['FROM'].str.contains("End")].index )
        cat_4['next_level'] = cat_4['level']+1
        cat_4.reset_index(drop=True, inplace=True)

        cat_5 = cat_4.groupby(['FROM', 'TO', 'level'])['counts'].sum().reset_index(name='counts').sort_values(by=['level'], axis =0)
        cat_5.reset_index(drop=True, inplace=True)

        cat_5['likelihood'] = 0

        total_counts_post = cat_5.groupby('FROM')['counts'].transform('sum')
        total_counts_prior = cat_5.groupby('TO')['counts'].transform('sum')

        cat_5['likelihood'].loc[cat_5.level >= 0] = (cat_5['counts'] / total_counts_post).loc[cat_5.level >= 0]
        cat_5['likelihood'].loc[cat_5.level < 0] = (cat_5['counts'] / total_counts_prior).loc[cat_5.level < 0]

        # Normal Behaviour Graph (NBG)
        G = nx.from_pandas_edgelist(cat_5, 'FROM', 'TO',  edge_attr='likelihood', create_using=nx.DiGraph())

        node_dic = cat_3[['from', 'FROM', 'level']]
        node_dic = node_dic.drop_duplicates()
        node_dic.reset_index(drop=True, inplace=True)
        nx.set_node_attributes(G, name= 'level', values= pd.Series( node_dic['level'].values, index=node_dic['FROM'] ).to_dict())
        nx.set_node_attributes(G, name= 'activity', values= pd.Series( node_dic['from'].values, index=node_dic['FROM'] ).to_dict())

        color = [ 'yellow' if data['activity'] == actset[i] else '#FF1744' for v, data in G.nodes(data=True)]

        pos = nx.multipartite_layout(G, subset_key= 'level')
        plt.figure(1, figsize=(12,9))
        plt.title("NBG for " + actset[i] )
        nx.draw(G, pos, with_labels= True, node_color = color)

        # 엣지 레이블 추가 (counts 값 표시)
        edge_labels = nx.get_edge_attributes(G, 'likelihood')
        nx.draw_networkx_edge_labels(G, pos, edge_labels=edge_labels, font_size=8, font_color= 'blue')
        NBGs = NBGs +[G]
        
        if vis:
            plt.savefig( my_path+'/image/' + actset[i] + '.png', format="PNG")
            
        plt.close()
        
    return NBGs


def discover_pattern(episode_history):
    """
    Analyzes the history of an RL episode to identify and label the
    repair patterns that were applied (e.g., insert, skip, rework).

    Args:
        episode_history (list): A list of tuples recording the steps of an episode.

    Returns:
        list: A list of dictionaries, where each dictionary represents an identified pattern.
              Example: [{'pattern': 'insert', 'loc': 3, 'activity': 'activityB'}, ...]
    """
    # Create a DataFrame from the episode history for easier analysis.
    df_explain = pd.DataFrame({'action': [hist[1] for hist in episode_history],
                               'loc': [hist[2] for hist in episode_history],
                               'label': [hist[3] for hist in episode_history],
                               'rework': [hist[4] for hist in episode_history]})
    df_explain['action_label'] = df_explain[['action', 'label']].apply(lambda x: str(x[0]) + '_' + str(x[1]), axis=1)
    df_explain['predict_patterns'] = None # None으로 초기화

    # --- Identify different patterns based on action types and context ---
    
    # Insert pattern (action is 'remove', which means an activity was inserted into the original log).
    loc_insert = (df_explain['action'] == 'remove')
    if sum(loc_insert) > 0:
        df_explain.loc[loc_insert, 'predict_patterns'] = df_explain.loc[loc_insert, ['loc', 'label']].apply(
            lambda x: {'pattern': 'insert', 'loc': x[0], 'activity': x[1]}, axis=1
        )

    # Skip pattern (action is 'inject', which means an activity was skipped from the original log).
    loc_skip = (df_explain['action'] == 'inject')
    if sum(loc_skip) > 0:
        df_explain.loc[loc_skip, 'predict_patterns'] = df_explain.loc[loc_skip, ['loc', 'label']].apply(
            lambda x: {'pattern': 'skip', 'loc': x[0], 'activity': x[1]}, axis=1
        )

    # Rework pattern.
    loc_rework = (df_explain['rework'] == True)
    if sum(loc_rework) > 0:
        df_explain.loc[loc_rework, 'predict_patterns'] = df_explain.loc[loc_rework, ['loc', 'label']].apply(
            lambda x: {'pattern': 'rework', 'loc': x[0], 'activity': x[1]}, axis=1
        )

    # Move pattern (an activity appears multiple times but not as a rework).
    loc_moved = (~loc_rework) & (df_explain['label'].duplicated(keep=False)) & (~df_explain['action_label'].duplicated(keep=False))
    if sum(loc_moved) > 0:
        df_explain.loc[loc_moved, 'predict_patterns'] = df_explain.loc[loc_moved, ['action', 'loc', 'label']].apply(
            lambda x: {'pattern': 'move', 'action': x[0], 'loc': x[1], 'activity': x[2]}, axis=1
        )

    # Replace pattern (different actions/activities at the same location).
    def check_different_actions_activities(group):
        if len(group) < 2: return False
        first_row = group.iloc[0]
        for _, row in group.iloc[1:].iterrows():
            if row['action'] != first_row['action'] and row['label'] != first_row['label']:
                return True
        return False
    
    matching_locs = df_explain.groupby('loc').filter(check_different_actions_activities)['loc'].unique()
    loc_replace = (~loc_rework) & (df_explain['loc'].isin(matching_locs))
    if sum(loc_replace) > 0:
        df_explain.loc[loc_replace, 'predict_patterns'] = df_explain.loc[loc_replace, ['action', 'label']].apply(
            lambda x: {'pattern': 'replace', 'action': x[0], 'activity': x[1]}, axis=1
        )

    predicted_patterns_list = df_explain['predict_patterns'].dropna().tolist()

    return str(predicted_patterns_list)

# def discover_pattern(episode_history):
#     df_explain = pd.DataFrame( {'action':[hist[1] for hist in episode_history], 
#                             'loc': [hist[2] for hist in episode_history],
#                             'label': [hist[3] for hist in episode_history],
#                             'rework': [hist[4] for hist in episode_history]})

#     df_explain['action_label'] = df_explain[['action', 'label']].apply(lambda x:  str(x[0]) + '_' + str(x[1]), axis=1)

#     df_explain['predict_patterns'] = ''
#     df_explain['predict_patterns_simple'] = ''

#     # insert 
#     loc_insert = (df_explain['action'] == 'remove')
#     if sum(loc_insert)>0:
#         df_explain.loc[loc_insert, ['predict_patterns']] = df_explain.loc[loc_insert, ['loc', 'label']].apply(lambda x: 'insert_'+ str(x[0]) + '_' + str(x[1]), axis=1)

#     # insert 
#     loc_skip = (df_explain['action'] == 'inject')
#     if sum(loc_skip)>0:
#         df_explain.loc[loc_skip, ['predict_patterns']] = df_explain.loc[loc_skip, ['loc', 'label']].apply(lambda x: 'skip_'+ str(x[0]) + '_' + str(x[1]), axis=1)

#     # rework
#     loc_rework = (df_explain['rework'] == True)
#     if sum(loc_rework)>0:
#         df_explain.loc[loc_rework, ['predict_patterns']] = df_explain.loc[loc_rework, ['loc', 'label']].apply(lambda x: 'rework_'+ str(x[0]) + '_' + str(x[1]), axis=1)

#     # move
#     loc_moved = (~loc_rework) & (df_explain['label'].duplicated(keep=False)) & (~df_explain['action_label'].duplicated(keep=False))
#     if sum(loc_moved)>0:
#         df_explain.loc[loc_moved, ['predict_patterns']] = df_explain.loc[loc_moved, ['action','loc', 'label']].apply(lambda x: 'move_'+ str(x[0]) + '_' + str(x[1]) + '_' + str(x[2]), axis=1)
#         df_explain.loc[loc_moved, ['predict_patterns_simple']] = df_explain.loc[loc_moved, ['label']].apply(lambda x: 'move_'+ str(x[0]) , axis=1)

#     # replace 
#     def check_different_actions_activities(group):
#         if len(group) < 2:
#             return False
#         first_row = group.iloc[0]
#         for index, row in group.iloc[1:].iterrows():
#             if row['action'] != first_row['action'] and row['label'] != first_row['label']:
#                 return True
#         return False

#     matching_locs = df_explain.groupby('loc').filter(check_different_actions_activities)['loc'].unique()
#     loc_replace = (~loc_rework) & (df_explain['loc'].isin(matching_locs))

#     if sum(loc_replace)>0:
#         df_explain.loc[loc_replace, ['predict_patterns']] = df_explain.loc[loc_replace, ['action', 'label']].apply(lambda x: 'replace_'+ str(x[0]) + '_' + str(x[1]), axis=1)


#     return str(df_explain['predict_patterns'].tolist())



def process_case_id_wrapper(case_id_for_processing, # 첫 번째 인자는 Pool.map에서 오는 반복요소
                        # 이하 고정 인자들
                        p_anomaly_nolabel,
                        p_filtered_actset,
                        p_act_freq,
                        p_actset,
                        p_NBGs,
                        p_num_epi, 
                        alpha):

    agent = QLearningAgent(p_filtered_actset ,  p_act_freq,learning_rate=0.1, discount_factor=0.9, exploration_rate=0.5, exploration_decay_rate=0.0001)
    obs_case = p_anomaly_nolabel.loc[p_anomaly_nolabel['case:concept:name']  == case_id_for_processing].reset_index(drop=True)

    env = P_BEAR_RL(obs_case, p_actset, p_filtered_actset, p_NBGs, alpha =alpha) # jh
    episodes = p_num_epi
    all_episode_histories = []

    for episode in range(episodes):
        state = env.reset()
        total_reward = 0
        steps = 0
        done = False
        game_over = False
        episode_history = []

        while not done and not game_over:
            action = agent.select_action(state)
            next_state, reward, done, game_over, info, loc, label_act, rework = env.step(action, max_step=8)

            agent.update_q_value(state, action, reward, next_state)
            agent.decay_exploration_rate()

            total_reward += reward
            steps += 1
            if not done and (label_act != None): #jh
                episode_history.append((state.copy(), env.action_labels[action].split('_')[0], loc, label_act, rework,reward, next_state.copy()))
            state = next_state

        if done and not game_over:
            adm_last = env.ADM(next_state, 0 ,p_NBGs )
            all_episode_histories.append((steps-1, np.sum(adm_last)/(len(adm_last)*len(adm_last[0])), episode_history))

    if len(all_episode_histories)>0:
        check_clean = [h[0] for h in all_episode_histories]
        if max(check_clean)>0:   
            shortest_episodes = [hist for hist in all_episode_histories if hist[0] == min(h[0] for h in all_episode_histories if h[0] > 0)] # 0 step 제외
            best_episode = min(shortest_episodes, key=lambda x: x[1])
            env.state = best_episode[2][-1][-1] 
            env.state['predict_patterns'] = discover_pattern(best_episode[2])
            case_result_df = env.state
            # env.render()  # for visualization
        else:
            state = env.init_state
            state['predict_patterns'] = ''
            case_result_df = state
    else:
        state = env.init_state
        state['predict_patterns'] = ''
        case_result_df = state

    return case_result_df



# 강화 학습 환경
class P_BEAR_RL:
    def __init__(self, case, actset, filtered_actset, NBGs, alpha):    
        self.init_state = case
        self.state = case
        self.filtered_actset = filtered_actset # new
        self.actset = actset
        self.alpha = alpha
        self.action_space = list(range(len(filtered_actset)+2))  # 0~1: skip, 2~40: insert  
        self.action_labels = ["remove_A", "remove_B"]  +  [f'inject_{item}' for item in filtered_actset]
        self.initial_anomalies = np.sum(self.ADM(case,  threshold=alpha, NBGs=NBGs), axis=0)
        self.current_step = 0
        self.NBGs =NBGs # new

    def ADM(self, obs_case, threshold, NBGs):
        actlist = obs_case['concept:name'].tolist()
        invalid_act = [act for act in actlist if act not in self.actset]

        v_matrix = []
        for order, act in enumerate(actlist):
            if act not in invalid_act:

                edge_inTrace = [tuple([actlist[temp] + '_' + str(temp-order), actlist[temp+1]+ '_' + str(temp+1-order)]) for temp in range(len(actlist)-1)]

                voting_NBG = NBGs[np.where(self.actset == act)[0][0]]
                edge_labels = nx.get_edge_attributes(voting_NBG, 'likelihood')
                valid_edge = []
                for edge, likelihood in edge_labels.items():
                    if likelihood > threshold:
                        valid_edge.append(edge)

                vote = [0 if temp in valid_edge else 1 for temp in edge_inTrace]
                past_vote = vote[0:order]
                post_vote = vote[order:len(vote)]

                past_vote = (np.cumsum(np.cumsum(past_vote[::-1])) == 1).astype(int)[::-1]
                post_vote = (np.cumsum(np.cumsum(post_vote)) == 1).astype(int)
                vote = past_vote.tolist() + post_vote.tolist() 
                v_matrix.append(vote)

        return v_matrix

    def loc_anomalies(self, mat):
        anomalous_edge = np.sum(self.ADM(obs_case= mat, threshold= self.alpha, NBGs = self.NBGs), axis=0)
        indices_greater_than_one = np.where(anomalous_edge > 1)[0].tolist()
        return indices_greater_than_one

    def reset(self):
        self.state = self.init_state.copy()
        self.current_step = 0
        return self.state

    def step(self, action, max_step = 30):
        self.current_step += 1
        reward = -1
        done = False
        game_over = False
        rework = False
        idx = 0
        act = None
        
        prev_state = self.state.copy()
        
        indices_greater_than_one = self.loc_anomalies(prev_state)

        if len(indices_greater_than_one) == 0:
            # reward = 10  # 목표 스텝 내 해결 시 큰 보상
            done = True
            return self.state, reward, done, game_over, {}, idx, act, rework
        else:
            if action == 0:  # remove
                loc= random.sample(indices_greater_than_one, 1)[0]
                if loc>0:
                    act = prev_state['concept:name'][loc]
                    idx = prev_state['order'][loc]
                    repaired_case = prev_state.drop(index= loc).reset_index(drop=True)
                    self.state = repaired_case
                    if act == prev_state['concept:name'][loc+1]:
                        rework = True
                        
            elif action ==1:# remove
                loc= random.sample(indices_greater_than_one, 1)[0]+1
                if loc < len(prev_state)-1:
                    act = prev_state['concept:name'][loc]
                    idx = prev_state['order'][loc]
                    repaired_case = prev_state.drop(index= loc).reset_index(drop=True)
                    self.state = repaired_case
                    if act == prev_state['concept:name'][loc-1]:
                        rework = True 
                        
                        
            elif action > 1:  # inject

                loc = random.sample(indices_greater_than_one, 1)[0]+1
                idx = prev_state['order'][loc-1] +1
                cut1 = prev_state.loc[0:(loc-1)]
                cut2 = prev_state.loc[loc:len(prev_state)]
                insert = prev_state.loc[0:0]
                insert['concept:name'] = self.filtered_actset[action-2]  
                act = self.filtered_actset[action-2]  
                repaired_case = pd.concat([cut1, insert, cut2]).reset_index(drop=True)
                self.state = repaired_case
                loc= loc-1
                
                
                
            indices_greater_than_one_new = self.loc_anomalies(self.state)  

            if len(indices_greater_than_one_new) < len(indices_greater_than_one):
                reward += 5 - self.current_step  # 10 * 0.5 * np.exp(-0.5 * self.current_step)
            elif len(indices_greater_than_one_new) == len(indices_greater_than_one):
                reward -= 1
            else:
                reward -= 5
                # game_over = True

            if self.current_step > max_step :  # 최대 스텝 제한
                game_over = True
                reward -= 10

            if len(self.state) < 4:
                game_over = True

            done = self._is_done() 

            return self.state, reward, done, game_over, {}, idx, act, rework


    def _is_done(self):
        indices_greater_than_one = self.loc_anomalies(self.state)
        return (indices_greater_than_one== 0)


    def render(self):
        # NumPy 배열로 변환
        matrix_np = np.array(self.ADM(self.state, threshold= self.alpha, NBGs = self.NBGs))

        # 빨강/흰색 컬러맵 생성
        colors = ['white', 'red']
        cmap = ListedColormap(colors)
        # 빨강/흰색 이미지로 출력
        plt.imshow(matrix_np, cmap=cmap, vmin=0, vmax=1)
        plt.title('Anomaly Detection Matrix')
        plt.colorbar(label='(0: normal, 1: anomaly)')
        plt.xticks([])  
        plt.yticks([])  
        plt.show()



class QLearningAgent:
    def __init__(self, filtered_actset , act_freq, learning_rate=0.1, discount_factor=0.9, exploration_rate=1.0, exploration_decay_rate=0.001):
        
        self.filtered_actset = filtered_actset
        self.action_space = list(range(len(filtered_actset)+2))  # 0~1: skip, 2~40: insert  
        self.act_freq = act_freq
        self.learning_rate = learning_rate
        self.discount_factor = discount_factor
        self.exploration_rate = exploration_rate
        self.exploration_decay_rate = exploration_decay_rate
        self.q_table = {} # 상태-행동 가치 함수

    def _get_state_key(self, state):
        # 이미지를 튜플로 변환하여 딕셔너리 키로 사용
        return tuple(state.flatten())

    def get_q_value(self, state, action):
        state_key = self._get_state_key(np.array(state['concept:name']))
        return self.q_table.get((state_key, action), 0.0)

    def update_q_value(self, state, action, reward, next_state):
        state_key = self._get_state_key(np.array(state['concept:name']))
        next_state_key = self._get_state_key(np.array(state['concept:name']))
        best_next_q = np.max([self.get_q_value(state, a) for a in self.action_space]) if next_state_key in self.q_table else 0.0
        old_q = self.get_q_value(state, action)
        new_q = old_q + self.learning_rate * (reward + self.discount_factor * best_next_q - old_q)
        self.q_table[(state_key, action)] = new_q

    def select_action(self, state):
        if random.random() < self.exploration_rate:
            # 탐험: 무작위 행동 선택
            if random.random() < 0.5: # remove
                return random.choice([0,1])
            else:
                return random.choices(range(2,len(self.filtered_actset)+2), weights= self.act_freq.prob, k=1)[0]
                # return random.choice(range(2,len(filtered_actset)+2))  #random.choice(self.action_space)
        else:
            # 활용: 현재 Q-테이블에서 가장 높은 가치를 가지는 행동 선택
            state_key = self._get_state_key(np.array(state['concept:name']))
            q_values = [self.get_q_value(state, a) for a in self.action_space]
            return np.argmax(q_values)

    def decay_exploration_rate(self):
        self.exploration_rate = max(0.01, self.exploration_rate * (1 - self.exploration_decay_rate))
        
        
        

class P_BEAR_DRL:
    def __init__(self, case, actset, filtered_actset, NBGs, alpha = 0):
        self.init_state = case
        self.state = case
        self.filtered_actset = filtered_actset # new
        self.actset = actset
        self.action_space = list(range(len(filtered_actset) + 2))  # 0~1: remove, 2+: insert
        self.action_labels = ["remove_A", "remove_B"]  +  [f'inject_{item}' for item in filtered_actset]
        self.initial_anomalies = np.sum(self.ADM(case,  threshold=alpha, NBGs=NBGs), axis=0)
        self.current_step = 0
        self.NBGs =NBGs # new
        self.alpha = alpha

    def ADM(self, obs_case, threshold, NBGs):
        actlist = obs_case['concept:name'].tolist()
        invalid_act = [act for act in actlist if act not in self.actset]
        v_matrix = []
        for order, act in enumerate(actlist):
            if act not in invalid_act:
                edge_inTrace = [tuple([actlist[temp] + '_' + str(temp - order), actlist[temp + 1] + '_' + str(temp + 1 - order)]) for temp in range(len(actlist) - 1)]
                voting_NBG = NBGs[np.where(self.actset == act)[0][0]]
                edge_labels = nx.get_edge_attributes(voting_NBG, 'likelihood') if voting_NBG else {}
                valid_edge = [edge for edge, likelihood in edge_labels.items() if likelihood > threshold]
                vote = [0 if temp in valid_edge else 1 for temp in edge_inTrace]
                past_vote = vote[:order]
                post_vote = vote[order:]
                past_vote = (np.cumsum(np.cumsum(past_vote[::-1])) == 1).astype(int)[::-1]
                post_vote = (np.cumsum(np.cumsum(post_vote)) == 1).astype(int)
                vote = past_vote.tolist() + post_vote.tolist()
                v_matrix.append(vote)
        return v_matrix

    def loc_anomalies(self, mat):
        anomalous_edge = np.sum(self.ADM(obs_case=mat, threshold= self.alpha, NBGs=self.NBGs), axis=0)
        indices_greater_than_one = np.where(anomalous_edge > 1)[0].tolist()
        return indices_greater_than_one

    def reset(self):
        self.state = self.init_state.copy()
        self.current_step = 0
        return self.state

    def _is_done(self):
        indices_greater_than_one = self.loc_anomalies(self.state)
        return (indices_greater_than_one== 0)
    
    def step(self, action, max_step=10):
        self.current_step += 1
        reward = -1
        done = False
        game_over = False
        rework = False
        idx = 0
        act = None
        
        prev_state = self.state.copy()
        indices_greater_than_one = self.loc_anomalies(prev_state)

        if not indices_greater_than_one:
            done = True
            return self.state, reward, done, game_over, {}, idx, act, rework
        else:
            if action == 0:  # remove
                loc= min(indices_greater_than_one)
                if loc>0:
                    act = prev_state['concept:name'][loc]
                    idx = prev_state['order'][loc]
                    repaired_case = prev_state.drop(index= loc).reset_index(drop=True)
                    self.state = repaired_case
                    if act == prev_state['concept:name'][loc+1]:
                        rework = True 
                
            elif action ==1:# remove
                loc= min(indices_greater_than_one)+1
                if loc < len(prev_state)-1:
                    act = prev_state['concept:name'][loc]
                    idx = prev_state['order'][loc]
                    repaired_case = prev_state.drop(index= loc).reset_index(drop=True)
                    self.state = repaired_case
                    if act == prev_state['concept:name'][loc-1]:
                        rework = True 
                        
            elif action > 1:  # inject

                loc = min(indices_greater_than_one)+1
                idx = prev_state['order'][loc-1]
                cut1 = prev_state.loc[0:(loc-1)]
                cut2 = prev_state.loc[loc:len(prev_state)]
                insert = prev_state.loc[0:0]
                insert['concept:name'] = self.filtered_actset[action-2]  
                act = self.filtered_actset[action-2]  
                repaired_case = pd.concat([cut1, insert, cut2]).reset_index(drop=True)
                self.state = repaired_case
                
                
            indices_greater_than_one_new = self.loc_anomalies(self.state)

            if len(indices_greater_than_one_new) < len(indices_greater_than_one):
                reward += 5 - self.current_step
            elif len(indices_greater_than_one_new) == len(indices_greater_than_one):
                reward -= 1
            else:
                reward -= 5

            if self.current_step > max_step:
                game_over = True
                reward -= 10

            if len(self.state) < 4:
                game_over = True

            done = self._is_done()

            return self.state, reward, done, game_over, {}, idx, act, rework

    def render(self):
        matrix_np = np.array(self.ADM(self.state, threshold= self.alpha, NBGs=self.NBGs))
        colors = ['white', 'red']
        cmap = ListedColormap(colors)
        plt.imshow(matrix_np, cmap=cmap, vmin=0, vmax=1)
        plt.title('Anomaly Detection Matrix')
        plt.colorbar(label='(0: normal, 1: anomaly)')
        plt.xticks([])
        plt.yticks([])
        plt.show()

